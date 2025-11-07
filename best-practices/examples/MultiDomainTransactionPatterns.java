package com.example.transaction;

import com.example.common.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 여러 도메인에 걸친 트랜잭션 처리 패턴 모음
 *
 * 주요 패턴:
 * 1. Application Service Pattern - 가장 일반적, 단일 트랜잭션으로 여러 도메인 조율
 * 2. Domain Event Pattern - 이벤트 기반으로 도메인 간 결합도 낮춤
 * 3. Saga Pattern - 분산 트랜잭션 처리 (보상 트랜잭션)
 * 4. Outbox Pattern - 트랜잭션과 메시지 발행의 원자성 보장
 */

// ============================================================================
// 패턴 1: Application Service Pattern (가장 많이 사용)
// ============================================================================

/**
 * 주문 생성 시나리오:
 * - User 도메인: 사용자 검증
 * - Order 도메인: 주문 생성
 * - Inventory 도메인: 재고 차감
 * - Payment 도메인: 결제 처리
 * - Notification 도메인: 알림 발송
 */

@Service
public class PlaceOrderApplicationService {

    // 여러 도메인 서비스를 조합
    private final UserDomainService userDomainService;
    private final OrderDomainService orderDomainService;
    private final InventoryDomainService inventoryDomainService;
    private final PaymentDomainService paymentDomainService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public PlaceOrderApplicationService(
        UserDomainService userDomainService,
        OrderDomainService orderDomainService,
        InventoryDomainService inventoryDomainService,
        PaymentDomainService paymentDomainService,
        NotificationService notificationService,
        ApplicationEventPublisher eventPublisher
    ) {
        this.userDomainService = userDomainService;
        this.orderDomainService = orderDomainService;
        this.inventoryDomainService = inventoryDomainService;
        this.paymentDomainService = paymentDomainService;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Application Service에서 단일 트랜잭션으로 여러 도메인을 조율
     *
     * 장점:
     * - 간단하고 이해하기 쉬움
     * - ACID 보장
     * - 롤백이 자동으로 처리됨
     *
     * 단점:
     * - 트랜잭션이 길어질 수 있음
     * - 외부 API 호출 시 문제 (타임아웃 등)
     * - 확장성 제한
     */
    @Transactional
    public Result<OrderResponse, OrderError> placeOrder(PlaceOrderRequest request) {

        // 1단계: 사용자 검증 (User 도메인)
        Result<User, UserError> userResult = userDomainService.validateUserForOrder(request.userId());
        if (userResult.isFailure()) {
            return Result.failure(OrderError.fromUserError(userResult));
        }
        User user = userResult.getOrThrow();

        // 2단계: 재고 확인 및 예약 (Inventory 도메인)
        Result<Void, InventoryError> inventoryResult =
            inventoryDomainService.reserveItems(request.items());

        if (inventoryResult.isFailure()) {
            // 자동 롤백 - 재고 예약 실패
            return Result.failure(OrderError.fromInventoryError(inventoryResult));
        }

        // 3단계: 주문 생성 (Order 도메인)
        Result<Order, OrderError> orderResult =
            orderDomainService.createOrder(user.getId(), request.items());

        if (orderResult.isFailure()) {
            // 자동 롤백 - 이전 단계들도 모두 롤백됨
            return Result.failure(orderResult);
        }
        Order order = orderResult.getOrThrow();

        // 4단계: 결제 처리 (Payment 도메인)
        Result<Payment, PaymentError> paymentResult =
            paymentDomainService.processPayment(
                order.getId(),
                request.paymentMethod(),
                order.getTotalAmount()
            );

        if (paymentResult.isFailure()) {
            // 자동 롤백 - 주문, 재고 예약 모두 롤백
            return Result.failure(OrderError.fromPaymentError(paymentResult));
        }
        Payment payment = paymentResult.getOrThrow();

        // 5단계: 주문 확정
        order.confirm(payment.getId());
        orderDomainService.save(order);

        // 6단계: 도메인 이벤트 발행 (트랜잭션 커밋 후 처리)
        eventPublisher.publishEvent(new OrderPlacedEvent(
            order.getId(),
            user.getId(),
            order.getTotalAmount()
        ));

        return Result.success(OrderResponse.from(order));
    }
}

// ============================================================================
// 패턴 2: Domain Event Pattern (이벤트 기반)
// ============================================================================

/**
 * 도메인 이벤트를 사용한 느슨한 결합
 *
 * 장점:
 * - 도메인 간 결합도 낮음
 * - 확장성 좋음
 * - 비동기 처리 가능
 *
 * 단점:
 * - 즉시 일관성(Immediate Consistency) 보장 안됨
 * - 디버깅이 어려울 수 있음
 * - 이벤트 순서 관리 필요
 */

// 도메인 이벤트 정의
record OrderPlacedEvent(
    Long orderId,
    Long userId,
    Money totalAmount,
    LocalDateTime occurredAt
) {
    public OrderPlacedEvent(Long orderId, Long userId, Money totalAmount) {
        this(orderId, userId, totalAmount, LocalDateTime.now());
    }
}

record PaymentCompletedEvent(
    Long paymentId,
    Long orderId,
    Money amount
) {}

record InventoryReservedEvent(
    Long orderId,
    List<OrderItem> items
) {}

// 이벤트 리스너들 (각 도메인에서 처리)
@Component
class OrderEventListeners {

    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;
    private final LoyaltyService loyaltyService;

    public OrderEventListeners(
        NotificationService notificationService,
        AnalyticsService analyticsService,
        LoyaltyService loyaltyService
    ) {
        this.notificationService = notificationService;
        this.analyticsService = analyticsService;
        this.loyaltyService = loyaltyService;
    }

    /**
     * TransactionalEventListener를 사용하면 트랜잭션 커밋 후에 이벤트 처리
     *
     * phase 옵션:
     * - AFTER_COMMIT (기본값): 트랜잭션 커밋 후
     * - AFTER_ROLLBACK: 롤백 후
     * - AFTER_COMPLETION: 완료 후 (커밋이든 롤백이든)
     * - BEFORE_COMMIT: 커밋 전
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // 알림 발송 (실패해도 주문은 이미 완료됨)
        notificationService.sendOrderConfirmation(event.orderId())
            .onFailure(error -> {
                // 로깅만 하고 계속 진행
                System.err.println("Failed to send notification: " + error);
            });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlacedForAnalytics(OrderPlacedEvent event) {
        // 분석 데이터 수집
        analyticsService.trackOrderPlaced(
            event.orderId(),
            event.userId(),
            event.totalAmount()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlacedForLoyalty(OrderPlacedEvent event) {
        // 포인트 적립 (별도 트랜잭션)
        loyaltyService.awardPoints(event.userId(), event.totalAmount());
    }
}

/**
 * 이벤트 기반 패턴 사용 예제
 */
@Service
class OrderServiceWithEvents {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderServiceWithEvents(
        OrderRepository orderRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Result<Order, OrderError> createOrder(Long userId, List<OrderItem> items) {
        // 주문 생성
        Result<Order, OrderError> orderResult = Order.create(userId, items);

        if (orderResult.isFailure()) {
            return orderResult;
        }

        Order order = orderResult.getOrThrow();
        Order saved = orderRepository.save(order);

        // 이벤트 발행 - 다른 도메인들이 반응
        eventPublisher.publishEvent(new OrderPlacedEvent(
            saved.getId(),
            userId,
            saved.getTotalAmount()
        ));

        // 주문 생성은 즉시 완료, 나머지는 이벤트로 비동기 처리
        return Result.success(saved);
    }
}

// ============================================================================
// 패턴 3: Saga Pattern (분산 트랜잭션)
// ============================================================================

/**
 * Saga Pattern: 긴 트랜잭션을 여러 개의 작은 트랜잭션으로 분할
 * 각 단계마다 보상 트랜잭션(Compensation) 정의
 *
 * 구현 방식:
 * 1. Choreography: 이벤트 기반으로 각 서비스가 독립적으로 반응
 * 2. Orchestration: 중앙 조율자(Orchestrator)가 각 단계를 관리
 *
 * 여기서는 Orchestration 방식 예제
 */

@Service
class OrderSagaOrchestrator {

    private final UserService userService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final SagaStateRepository sagaStateRepository;

    public OrderSagaOrchestrator(
        UserService userService,
        InventoryService inventoryService,
        PaymentService paymentService,
        OrderService orderService,
        SagaStateRepository sagaStateRepository
    ) {
        this.userService = userService;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.sagaStateRepository = sagaStateRepository;
    }

    /**
     * Saga 실행
     * 각 단계를 순차적으로 실행하고, 실패 시 이전 단계들을 보상
     */
    public Result<Order, OrderError> executeOrderSaga(PlaceOrderRequest request) {
        // Saga 상태 생성
        SagaState saga = SagaState.create("ORDER_SAGA", request);
        sagaStateRepository.save(saga);

        try {
            // Step 1: 사용자 검증
            saga.recordStep("VALIDATE_USER", "STARTED");
            Result<User, UserError> userResult = userService.validateUser(request.userId());
            if (userResult.isFailure()) {
                saga.recordStep("VALIDATE_USER", "FAILED");
                sagaStateRepository.save(saga);
                return Result.failure(OrderError.fromUserError(userResult));
            }
            saga.recordStep("VALIDATE_USER", "COMPLETED");

            // Step 2: 재고 예약
            saga.recordStep("RESERVE_INVENTORY", "STARTED");
            Result<String, InventoryError> reservationResult =
                inventoryService.reserveInventory(request.items());

            if (reservationResult.isFailure()) {
                saga.recordStep("RESERVE_INVENTORY", "FAILED");
                // 보상 트랜잭션 실행
                compensate(saga);
                return Result.failure(OrderError.fromInventoryError(reservationResult));
            }
            String reservationId = reservationResult.getOrThrow();
            saga.recordStep("RESERVE_INVENTORY", "COMPLETED", reservationId);

            // Step 3: 결제 처리
            saga.recordStep("PROCESS_PAYMENT", "STARTED");
            Result<Payment, PaymentError> paymentResult =
                paymentService.charge(request.paymentMethod(), calculateTotal(request.items()));

            if (paymentResult.isFailure()) {
                saga.recordStep("PROCESS_PAYMENT", "FAILED");
                // 보상: 재고 예약 취소
                compensate(saga);
                return Result.failure(OrderError.fromPaymentError(paymentResult));
            }
            Payment payment = paymentResult.getOrThrow();
            saga.recordStep("PROCESS_PAYMENT", "COMPLETED", payment.getId().toString());

            // Step 4: 주문 생성
            saga.recordStep("CREATE_ORDER", "STARTED");
            Result<Order, OrderError> orderResult =
                orderService.createConfirmedOrder(request.userId(), request.items(), payment.getId());

            if (orderResult.isFailure()) {
                saga.recordStep("CREATE_ORDER", "FAILED");
                // 보상: 결제 취소 + 재고 예약 취소
                compensate(saga);
                return orderResult;
            }
            Order order = orderResult.getOrThrow();
            saga.recordStep("CREATE_ORDER", "COMPLETED", order.getId().toString());

            // Saga 완료
            saga.complete();
            sagaStateRepository.save(saga);

            return Result.success(order);

        } catch (Exception e) {
            // 예상치 못한 에러 발생 시 보상
            saga.recordError(e.getMessage());
            compensate(saga);
            return Result.failure(new OrderError.SagaFailed(e.getMessage()));
        }
    }

    /**
     * 보상 트랜잭션 실행
     * 완료된 단계들을 역순으로 되돌림
     */
    private void compensate(SagaState saga) {
        List<SagaStep> completedSteps = saga.getCompletedSteps();

        // 역순으로 보상 실행
        for (int i = completedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = completedSteps.get(i);

            try {
                switch (step.name()) {
                    case "RESERVE_INVENTORY" -> {
                        String reservationId = step.data();
                        inventoryService.cancelReservation(reservationId);
                        saga.recordCompensation(step.name(), "COMPENSATED");
                    }
                    case "PROCESS_PAYMENT" -> {
                        String paymentId = step.data();
                        paymentService.refund(paymentId);
                        saga.recordCompensation(step.name(), "COMPENSATED");
                    }
                    case "CREATE_ORDER" -> {
                        String orderId = step.data();
                        orderService.cancelOrder(Long.parseLong(orderId));
                        saga.recordCompensation(step.name(), "COMPENSATED");
                    }
                }
            } catch (Exception e) {
                // 보상 실패 기록 (수동 개입 필요)
                saga.recordCompensation(step.name(), "COMPENSATION_FAILED: " + e.getMessage());
            }
        }

        saga.markAsCompensated();
        sagaStateRepository.save(saga);
    }

    private Money calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
}

// Saga 상태 관리
record SagaStep(String name, String status, String data, LocalDateTime timestamp) {}

class SagaState {
    private String sagaId;
    private String sagaType;
    private String status; // STARTED, COMPLETED, COMPENSATING, COMPENSATED, FAILED
    private List<SagaStep> steps;
    private String errorMessage;
    private Object payload;

    public static SagaState create(String sagaType, Object payload) {
        SagaState saga = new SagaState();
        saga.sagaId = java.util.UUID.randomUUID().toString();
        saga.sagaType = sagaType;
        saga.status = "STARTED";
        saga.steps = new java.util.ArrayList<>();
        saga.payload = payload;
        return saga;
    }

    public void recordStep(String stepName, String status) {
        recordStep(stepName, status, null);
    }

    public void recordStep(String stepName, String status, String data) {
        steps.add(new SagaStep(stepName, status, data, LocalDateTime.now()));
    }

    public void recordError(String message) {
        this.errorMessage = message;
        this.status = "FAILED";
    }

    public void complete() {
        this.status = "COMPLETED";
    }

    public void markAsCompensated() {
        this.status = "COMPENSATED";
    }

    public void recordCompensation(String stepName, String status) {
        steps.add(new SagaStep("COMPENSATE_" + stepName, status, null, LocalDateTime.now()));
    }

    public List<SagaStep> getCompletedSteps() {
        return steps.stream()
            .filter(step -> step.status().equals("COMPLETED"))
            .toList();
    }

    // Getters
    public String getSagaId() { return sagaId; }
    public String getStatus() { return status; }
}

interface SagaStateRepository {
    void save(SagaState saga);
    Optional<SagaState> findById(String sagaId);
}

// ============================================================================
// 패턴 4: Outbox Pattern (메시지 발행 원자성 보장)
// ============================================================================

/**
 * Outbox Pattern: 트랜잭션과 이벤트 발행의 원자성 보장
 *
 * 문제:
 * - DB 트랜잭션 커밋 후 메시지 브로커에 이벤트 발행 시 실패하면 데이터 불일치
 *
 * 해결:
 * - 이벤트를 Outbox 테이블에 저장 (같은 트랜잭션)
 * - 별도 프로세스가 Outbox를 폴링하여 메시지 브로커에 발행
 */

@Service
class OrderServiceWithOutbox {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    public OrderServiceWithOutbox(
        OrderRepository orderRepository,
        OutboxRepository outboxRepository
    ) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
    }

    /**
     * 주문 생성 + Outbox에 이벤트 저장 (단일 트랜잭션)
     */
    @Transactional
    public Result<Order, OrderError> createOrder(Long userId, List<OrderItem> items) {
        // 1. 주문 생성
        Result<Order, OrderError> orderResult = Order.create(userId, items);
        if (orderResult.isFailure()) {
            return orderResult;
        }

        Order order = orderResult.getOrThrow();
        Order saved = orderRepository.save(order);

        // 2. Outbox에 이벤트 저장 (같은 트랜잭션)
        OutboxEvent outboxEvent = OutboxEvent.create(
            "OrderPlaced",
            "order",
            saved.getId().toString(),
            new OrderPlacedEventPayload(saved.getId(), userId, saved.getTotalAmount())
        );
        outboxRepository.save(outboxEvent);

        // 트랜잭션 커밋 시 주문과 이벤트가 모두 저장되거나 모두 롤백됨
        return Result.success(saved);
    }
}

// Outbox 엔티티
class OutboxEvent {
    private Long id;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private String payload; // JSON
    private LocalDateTime createdAt;
    private String status; // PENDING, PUBLISHED, FAILED

    public static OutboxEvent create(
        String eventType,
        String aggregateType,
        String aggregateId,
        Object payload
    ) {
        OutboxEvent event = new OutboxEvent();
        event.eventType = eventType;
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.payload = serializeToJson(payload);
        event.createdAt = LocalDateTime.now();
        event.status = "PENDING";
        return event;
    }

    public void markAsPublished() {
        this.status = "PUBLISHED";
    }

    public void markAsFailed() {
        this.status = "FAILED";
    }

    private static String serializeToJson(Object obj) {
        // JSON 직렬화 (Jackson 등 사용)
        return "{}"; // simplified
    }

    // Getters
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
}

interface OutboxRepository {
    void save(OutboxEvent event);
    List<OutboxEvent> findPendingEvents();
    void update(OutboxEvent event);
}

record OrderPlacedEventPayload(Long orderId, Long userId, Money totalAmount) {}

/**
 * Outbox Publisher: 별도 스케줄러가 주기적으로 실행
 */
@Component
class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;
    private final MessageBroker messageBroker; // Kafka, RabbitMQ 등

    public OutboxEventPublisher(
        OutboxRepository outboxRepository,
        MessageBroker messageBroker
    ) {
        this.outboxRepository = outboxRepository;
        this.messageBroker = messageBroker;
    }

    /**
     * 주기적으로 실행 (예: @Scheduled)
     */
    // @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents();

        for (OutboxEvent event : pendingEvents) {
            try {
                // 메시지 브로커에 발행
                messageBroker.publish(event.getEventType(), event.getPayload());

                // 성공 시 상태 업데이트
                event.markAsPublished();
                outboxRepository.update(event);

            } catch (Exception e) {
                // 실패 시 상태 업데이트
                event.markAsFailed();
                outboxRepository.update(event);

                // 로깅 및 알림
                System.err.println("Failed to publish event: " + event.getEventType());
            }
        }
    }
}

// ============================================================================
// 보조 인터페이스 및 클래스들
// ============================================================================

// Domain Services
interface UserDomainService {
    Result<User, UserError> validateUserForOrder(Long userId);
}

interface OrderDomainService {
    Result<Order, OrderError> createOrder(Long userId, List<OrderItem> items);
    void save(Order order);
}

interface InventoryDomainService {
    Result<Void, InventoryError> reserveItems(List<OrderItem> items);
}

interface PaymentDomainService {
    Result<Payment, PaymentError> processPayment(Long orderId, String paymentMethod, Money amount);
}

interface NotificationService {
    Result<Void, NotificationError> sendOrderConfirmation(Long orderId);
}

// Services
interface UserService {
    Result<User, UserError> validateUser(Long userId);
}

interface InventoryService {
    Result<String, InventoryError> reserveInventory(List<OrderItem> items);
    void cancelReservation(String reservationId);
}

interface PaymentService {
    Result<Payment, PaymentError> charge(String paymentMethod, Money amount);
    void refund(String paymentId);
}

interface OrderService {
    Result<Order, OrderError> createConfirmedOrder(Long userId, List<OrderItem> items, String paymentId);
    void cancelOrder(Long orderId);
}

interface AnalyticsService {
    void trackOrderPlaced(Long orderId, Long userId, Money amount);
}

interface LoyaltyService {
    void awardPoints(Long userId, Money amount);
}

interface MessageBroker {
    void publish(String topic, String message);
}

// Models
class User {
    private Long id;
    private String name;
    public Long getId() { return id; }
}

class Order {
    private Long id;
    private Long userId;
    private List<OrderItem> items;
    private Money totalAmount;
    private String status;

    public static Result<Order, OrderError> create(Long userId, List<OrderItem> items) {
        Order order = new Order();
        order.userId = userId;
        order.items = items;
        order.totalAmount = calculateTotal(items);
        order.status = "PENDING";
        return Result.success(order);
    }

    public void confirm(String paymentId) {
        this.status = "CONFIRMED";
    }

    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    public Long getId() { return id; }
    public Money getTotalAmount() { return totalAmount; }
}

record OrderItem(String productId, String productName, Money unitPrice, int quantity) {
    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }
}

class Payment {
    private String id;
    private Money amount;
    public String getId() { return id; }
}

record Money(BigDecimal amount, String currency) {
    public static final Money ZERO = new Money(BigDecimal.ZERO, "KRW");

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }
}

// Repositories
interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
}

// DTOs
record PlaceOrderRequest(Long userId, List<OrderItem> items, String paymentMethod) {}
record OrderResponse(Long orderId, Money totalAmount, String status) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getTotalAmount(), "SUCCESS");
    }
}

// Errors
sealed interface OrderError {
    record UserValidationFailed(String message) implements OrderError {}
    record InventoryNotAvailable(String message) implements OrderError {}
    record PaymentFailed(String message) implements OrderError {}
    record SagaFailed(String message) implements OrderError {}

    static OrderError fromUserError(Result<User, UserError> result) {
        return new UserValidationFailed("User validation failed");
    }

    static OrderError fromInventoryError(Result<?, InventoryError> result) {
        return new InventoryNotAvailable("Inventory not available");
    }

    static OrderError fromPaymentError(Result<Payment, PaymentError> result) {
        return new PaymentFailed("Payment failed");
    }
}

sealed interface UserError {}
sealed interface InventoryError {}
sealed interface PaymentError {}
sealed interface NotificationError {}

/**
 * ============================================================================
 * 패턴 선택 가이드
 * ============================================================================
 *
 * 1. Application Service Pattern (가장 일반적)
 *    - 언제: 단순한 CRUD, 짧은 트랜잭션, 모놀리스 환경
 *    - 장점: 간단, ACID 보장, 이해하기 쉬움
 *    - 단점: 확장성 제한, 긴 트랜잭션 시 성능 문제
 *
 * 2. Domain Event Pattern
 *    - 언제: 도메인 간 결합도를 낮추고 싶을 때, 비동기 처리가 필요할 때
 *    - 장점: 낮은 결합도, 확장 용이, 유연함
 *    - 단점: 즉시 일관성 보장 안됨, 디버깅 어려움
 *
 * 3. Saga Pattern
 *    - 언제: 분산 시스템, 마이크로서비스, 긴 트랜잭션
 *    - 장점: 확장성, 각 서비스 독립적
 *    - 단점: 복잡도 높음, 보상 로직 필요, 디버깅 어려움
 *
 * 4. Outbox Pattern
 *    - 언제: 이벤트 유실 방지가 중요할 때, 메시지 발행 원자성 필요할 때
 *    - 장점: 원자성 보장, 안정적
 *    - 단점: 추가 테이블 필요, 폴링 오버헤드
 *
 * ============================================================================
 * 실무 추천 조합
 * ============================================================================
 *
 * 🏆 Most Common: Application Service + Domain Events
 * - Application Service로 핵심 트랜잭션 처리
 * - Domain Events로 부가 작업 처리 (알림, 분석 등)
 *
 * 예시:
 * @Transactional
 * public Result<Order> placeOrder(Request req) {
 *     // 핵심 로직 (동기, 트랜잭션 내)
 *     Order order = createOrder(req);
 *     reserveInventory(order);
 *     processPayment(order);
 *
 *     // 부가 작업 (비동기, 이벤트)
 *     eventPublisher.publish(new OrderPlacedEvent(order));
 *
 *     return order;
 * }
 *
 * @TransactionalEventListener(phase = AFTER_COMMIT)
 * void handleOrderPlaced(OrderPlacedEvent event) {
 *     sendNotification(event);  // 실패해도 주문은 완료됨
 *     updateAnalytics(event);
 *     awardLoyaltyPoints(event);
 * }
 *
 * ============================================================================
 */
