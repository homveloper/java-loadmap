package com.example.application;

import com.example.common.Result;
import com.example.domain.model.*;
import com.example.domain.valueobject.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Layer 예제
 *
 * 핵심 원칙:
 * 1. Constructor Injection으로 의존성 주입
 * 2. 인터페이스에 의존 (구현체가 아닌)
 * 3. Composition over Inheritance
 * 4. UseCase별로 분리
 * 5. Result 패턴으로 에러 처리
 */

// ============================================================================
// Repository Interfaces (Domain Layer)
// ============================================================================

/**
 * Repository는 인터페이스로 정의 (구현은 Infrastructure Layer에)
 */
public interface UserRepository {
    Optional<User> findById(UserId userId);
    Optional<User> findByEmail(Email email);
    User save(User user);
    List<User> findByStatus(UserStatus status);
    boolean existsByEmail(Email email);
}

public interface OrderRepository {
    Optional<Order> findByOrderNumber(OrderNumber orderNumber);
    List<Order> findByUserId(UserId userId);
    Order save(Order order);
    int getNextOrderSequence();
}

// ============================================================================
// Domain Services (복잡한 비즈니스 로직)
// ============================================================================

/**
 * UserDomainService - 도메인 서비스
 *
 * 단일 엔티티로 처리할 수 없는 도메인 로직을 처리
 */
@Service
public class UserDomainService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor Injection
    public UserDomainService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 사용자 등록 (이메일 중복 검증 포함)
     */
    public Result<User, UserError> registerUser(
        String username,
        String email,
        String rawPassword
    ) {
        // 1. 도메인 객체 생성 (팩토리 메서드)
        Result<User, UserError> userResult = User.create(username, email, rawPassword);

        // 2. 이메일 중복 검증 (도메인 규칙)
        return userResult.flatMap(user -> {
            if (userRepository.existsByEmail(user.getEmail())) {
                return Result.failure(new UserError.EmailAlreadyExists(user.getEmail()));
            }

            // 3. 비밀번호 인코딩 (인프라 관심사지만 도메인 서비스에서 처리)
            String encoded = passwordEncoder.encode(rawPassword);
            user.setPassword(encoded);  // package-private setter

            return Result.success(user);
        });
    }

    /**
     * 로그인 처리
     */
    public Result<User, UserError> authenticate(
        String email,
        String password
    ) {
        Email userEmail;
        try {
            userEmail = Email.of(email);
        } catch (IllegalArgumentException e) {
            return Result.failure(new UserError.InvalidInput("Invalid email format"));
        }

        return userRepository.findByEmail(userEmail)
            .map(user -> {
                // 비밀번호 검증
                if (!passwordEncoder.matches(password, user.getHashedPassword())) {
                    // 실패 기록
                    user.recordFailedLogin();
                    userRepository.save(user);
                    return Result.<User, UserError>failure(
                        new UserError.InvalidPassword("Incorrect password")
                    );
                }

                // 로그인 성공 기록
                return user.recordSuccessfulLogin()
                    .map(v -> {
                        userRepository.save(user);
                        return user;
                    });
            })
            .orElse(Result.failure(new UserError.NotFound(null)));
    }
}

// ============================================================================
// Application Services (UseCase)
// ============================================================================

/**
 * CreateUserUseCase - 사용자 생성 유스케이스
 *
 * Application Layer에서 여러 도메인 서비스와 인프라 서비스를 조율
 */
@Service
public class CreateUserUseCase {

    // Composition: 여러 서비스를 조합
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EventPublisher eventPublisher;

    // Constructor Injection - 모든 의존성 명시
    public CreateUserUseCase(
        UserDomainService userDomainService,
        UserRepository userRepository,
        EmailService emailService,
        EventPublisher eventPublisher
    ) {
        this.userDomainService = userDomainService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Result<UserResponse, UserError> execute(CreateUserRequest request) {
        // 1. 도메인 로직 실행
        Result<User, UserError> userResult = userDomainService.registerUser(
            request.username(),
            request.email(),
            request.password()
        );

        // 2. 저장 및 부가 작업
        return userResult.map(user -> {
            // 저장
            User saved = userRepository.save(user);

            // 이메일 발송 (Best Effort - 실패해도 사용자 생성은 성공)
            emailService.sendWelcomeEmail(saved)
                .onFailure(error -> {
                    // 로깅만 하고 계속 진행
                    System.err.println("Failed to send welcome email: " + error);
                });

            // 도메인 이벤트 발행
            eventPublisher.publish(new UserCreatedEvent(saved.getId(), saved.getEmail()));

            // DTO로 변환하여 반환
            return UserResponse.from(saved);
        });
    }
}

/**
 * PlaceOrderUseCase - 주문 생성 유스케이스
 */
@Service
public class PlaceOrderUseCase {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final EventPublisher eventPublisher;

    public PlaceOrderUseCase(
        UserRepository userRepository,
        OrderRepository orderRepository,
        PaymentService paymentService,
        InventoryService inventoryService,
        EventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Result<OrderResponse, OrderError> execute(PlaceOrderRequest request) {
        UserId userId = UserId.of(request.userId());

        // 1. 사용자 검증
        Result<User, UserError> userResult = userRepository.findById(userId)
            .map(user -> {
                if (!user.canPlaceOrder()) {
                    return Result.<User, UserError>failure(
                        new UserError.OperationNotAllowed("User cannot place order: " + userId)
                    );
                }
                return Result.<User, UserError>success(user);
            })
            .orElse(Result.failure(new UserError.NotFound(userId)));

        // userResult를 OrderError로 변환
        if (userResult.isFailure()) {
            return Result.failure(new OrderError.InvalidInput(
                "User validation failed: " + userResult
            ));
        }

        // 2. 주문 아이템 생성
        List<OrderItem> items = request.items().stream()
            .map(item -> new OrderItem(
                item.productId(),
                item.productName(),
                Money.krw(item.unitPrice()),
                item.quantity()
            ))
            .toList();

        // 3. 주문 생성
        int sequence = orderRepository.getNextOrderSequence();
        Result<Order, OrderError> orderResult = Order.create(userId, items, sequence);

        return orderResult.flatMap(order -> {
            // 4. 재고 확인
            Result<Void, InventoryError> inventoryResult =
                inventoryService.checkAndReserve(items);

            if (inventoryResult.isFailure()) {
                return Result.failure(new OrderError.InvalidInput(
                    "Insufficient inventory"
                ));
            }

            // 5. 결제 처리
            Result<Payment, PaymentError> paymentResult =
                paymentService.processPayment(
                    order.getOrderNumber(),
                    order.getTotalAmount(),
                    request.paymentMethod()
                );

            if (paymentResult.isFailure()) {
                // 재고 롤백
                inventoryService.releaseReservation(items);
                return Result.failure(new OrderError.InvalidInput(
                    "Payment failed"
                ));
            }

            // 6. 주문 확정
            order.confirm();
            Order saved = orderRepository.save(order);

            // 7. 이벤트 발행
            eventPublisher.publish(new OrderPlacedEvent(
                saved.getOrderNumber(),
                saved.getUserId(),
                saved.getTotalAmount()
            ));

            return Result.success(OrderResponse.from(saved));
        });
    }
}

/**
 * CancelOrderUseCase - 주문 취소 유스케이스
 */
@Service
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final RefundService refundService;
    private final InventoryService inventoryService;
    private final EventPublisher eventPublisher;

    public CancelOrderUseCase(
        OrderRepository orderRepository,
        RefundService refundService,
        InventoryService inventoryService,
        EventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.refundService = refundService;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Result<Void, OrderError> execute(String orderNumberStr) {
        OrderNumber orderNumber = OrderNumber.of(orderNumberStr);

        return orderRepository.findByOrderNumber(orderNumber)
            .map(order -> {
                // 1. 주문 취소 (도메인 로직)
                return order.cancel()
                    .flatMap(v -> {
                        // 2. 환불 처리
                        refundService.processRefund(order);

                        // 3. 재고 복구
                        inventoryService.restoreInventory(order.getItems());

                        // 4. 저장
                        orderRepository.save(order);

                        // 5. 이벤트 발행
                        eventPublisher.publish(new OrderCancelledEvent(
                            order.getOrderNumber(),
                            order.getTotalAmount()
                        ));

                        return Result.<Void, OrderError>successVoid();
                    });
            })
            .orElse(Result.failure(new OrderError.NotFound(orderNumber)));
    }
}

// ============================================================================
// Infrastructure Services (Interfaces)
// ============================================================================

/**
 * 외부 서비스들은 인터페이스로 정의
 * 구현은 Infrastructure Layer에
 */
public interface EmailService {
    Result<Void, EmailError> sendWelcomeEmail(User user);
    Result<Void, EmailError> sendOrderConfirmation(Order order);
}

public interface PaymentService {
    Result<Payment, PaymentError> processPayment(
        OrderNumber orderNumber,
        Money amount,
        String paymentMethod
    );
}

public interface RefundService {
    Result<Void, RefundError> processRefund(Order order);
}

public interface InventoryService {
    Result<Void, InventoryError> checkAndReserve(List<OrderItem> items);
    void releaseReservation(List<OrderItem> items);
    void restoreInventory(List<OrderItem> items);
}

public interface EventPublisher {
    void publish(DomainEvent event);
}

public interface PasswordEncoder {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

// ============================================================================
// DTOs (Data Transfer Objects)
// ============================================================================

/**
 * Request/Response DTOs
 * Records를 사용하여 불변성 보장
 */
public record CreateUserRequest(
    String username,
    String email,
    String password
) {}

public record UserResponse(
    Long id,
    String username,
    String email,
    String status
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId() != null ? user.getId().value() : null,
            user.getUsername(),
            user.getEmail().value(),
            user.getStatus().name()
        );
    }
}

public record PlaceOrderRequest(
    Long userId,
    List<OrderItemRequest> items,
    String paymentMethod
) {}

public record OrderItemRequest(
    String productId,
    String productName,
    double unitPrice,
    int quantity
) {}

public record OrderResponse(
    String orderNumber,
    Long userId,
    List<OrderItemResponse> items,
    String totalAmount,
    String status
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getOrderNumber().value(),
            order.getUserId().value(),
            order.getItems().stream()
                .map(item -> new OrderItemResponse(
                    item.productId(),
                    item.productName(),
                    item.unitPrice().toString(),
                    item.quantity()
                ))
                .toList(),
            order.getTotalAmount().toString(),
            order.getStatus().name()
        );
    }
}

public record OrderItemResponse(
    String productId,
    String productName,
    String unitPrice,
    int quantity
) {}

// ============================================================================
// Domain Events
// ============================================================================

public sealed interface DomainEvent permits
    UserCreatedEvent,
    OrderPlacedEvent,
    OrderCancelledEvent {

    String getEventType();
    java.time.LocalDateTime getOccurredAt();
}

public record UserCreatedEvent(
    UserId userId,
    Email email,
    java.time.LocalDateTime occurredAt
) implements DomainEvent {

    public UserCreatedEvent(UserId userId, Email email) {
        this(userId, email, java.time.LocalDateTime.now());
    }

    @Override
    public String getEventType() {
        return "USER_CREATED";
    }

    @Override
    public java.time.LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

public record OrderPlacedEvent(
    OrderNumber orderNumber,
    UserId userId,
    Money totalAmount,
    java.time.LocalDateTime occurredAt
) implements DomainEvent {

    public OrderPlacedEvent(OrderNumber orderNumber, UserId userId, Money totalAmount) {
        this(orderNumber, userId, totalAmount, java.time.LocalDateTime.now());
    }

    @Override
    public String getEventType() {
        return "ORDER_PLACED";
    }

    @Override
    public java.time.LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

public record OrderCancelledEvent(
    OrderNumber orderNumber,
    Money refundAmount,
    java.time.LocalDateTime occurredAt
) implements DomainEvent {

    public OrderCancelledEvent(OrderNumber orderNumber, Money refundAmount) {
        this(orderNumber, refundAmount, java.time.LocalDateTime.now());
    }

    @Override
    public String getEventType() {
        return "ORDER_CANCELLED";
    }

    @Override
    public java.time.LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

// ============================================================================
// Additional Error Types
// ============================================================================

sealed interface EmailError permits EmailError.SendFailed {
    record SendFailed(String reason) implements EmailError {}
}

sealed interface PaymentError permits PaymentError.InsufficientFunds, PaymentError.ProcessingFailed {
    record InsufficientFunds() implements PaymentError {}
    record ProcessingFailed(String reason) implements PaymentError {}
}

sealed interface RefundError permits RefundError.ProcessingFailed {
    record ProcessingFailed(String reason) implements RefundError {}
}

sealed interface InventoryError permits InventoryError.InsufficientStock {
    record InsufficientStock(String productId) implements InventoryError {}
}

record Payment(String paymentId, Money amount) {}

// ============================================================================
// 핵심 포인트 정리
// ============================================================================

/**
 * Service Layer 설계 원칙:
 *
 * 1. Constructor Injection
 *    - Field injection 대신 constructor injection 사용
 *    - 불변성 보장 (final 필드)
 *    - 테스트 용이성
 *
 * 2. 인터페이스 의존
 *    - 구체 클래스가 아닌 인터페이스에 의존
 *    - Repository, EmailService 등 모두 인터페이스
 *    - 구현체는 Infrastructure Layer에서 주입
 *
 * 3. Composition over Inheritance
 *    - BaseService 상속 대신 서비스 조합
 *    - 각 서비스는 독립적으로 테스트 가능
 *
 * 4. UseCase 분리
 *    - 하나의 UseCase = 하나의 클래스
 *    - CreateUserUseCase, PlaceOrderUseCase 등
 *    - 단일 책임 원칙 준수
 *
 * 5. Result 패턴
 *    - Exception 대신 Result로 에러 처리
 *    - 명시적이고 예측 가능한 에러 흐름
 *
 * 6. Transaction 관리
 *    - @Transactional은 UseCase에서만
 *    - 도메인 서비스는 트랜잭션 무관
 *
 * 7. 도메인 이벤트
 *    - 도메인 로직 실행 후 이벤트 발행
 *    - 느슨한 결합 유지
 */
