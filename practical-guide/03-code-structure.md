# 실용적인 코드 구조

> 확장 가능하고 유지보수 쉬운 구조 만들기

## 목차
- [레이어 분리 전략](#레이어-분리-전략)
- [도메인 모델 vs Entity vs DTO](#도메인-모델-vs-entity-vs-dto)
- [의존성 관리](#의존성-관리)
- [패키지 구조](#패키지-구조)
- [언제 추상화할 것인가](#언제-추상화할-것인가)

---

## 레이어 분리 전략

### 전통적인 3-Layer Architecture

```
┌─────────────────────────────────┐
│     Presentation Layer          │  ← Controller, API
│  (사용자 인터페이스)              │
└─────────────────────────────────┘
              ↓
┌─────────────────────────────────┐
│     Business Layer              │  ← Service, 비즈니스 로직
│  (비즈니스 로직)                  │
└─────────────────────────────────┘
              ↓
┌─────────────────────────────────┐
│     Data Access Layer           │  ← Repository, Database
│  (데이터 접근)                    │
└─────────────────────────────────┘
```

### 실제 프로덕션 구조

```
┌─────────────────────────────────┐
│  API Layer (Controller)         │  ← HTTP 요청/응답, 입력 검증
└─────────────────────────────────┘
              ↓
┌─────────────────────────────────┐
│  Application Layer (UseCase)    │  ← 유스케이스 조율, 트랜잭션
└─────────────────────────────────┘
              ↓
┌─────────────────────────────────┐
│  Domain Layer (Entity, Service) │  ← 비즈니스 로직, 도메인 규칙
└─────────────────────────────────┘
              ↓
┌─────────────────────────────────┐
│  Infrastructure Layer           │  ← DB, 외부 API, 메시징
└─────────────────────────────────┘
```

### 예시: 주문 처리

```java
// ============================================
// 1. API Layer - HTTP 인터페이스
// ============================================
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Result<Order> result = createOrderUseCase.execute(
            request.customerId(),
            request.items()
        );

        return switch (result) {
            case Result.Success<Order> s ->
                ResponseEntity.ok(OrderResponse.from(s.value()));
            case Result.Failure<Order> f ->
                ResponseEntity.badRequest().body(
                    new ErrorResponse(f.error().code(), f.error().message())
                );
        };
    }
}

// DTO
public record CreateOrderRequest(
    Long customerId,
    List<OrderItemRequest> items
) {}

public record OrderResponse(
    Long id,
    String status,
    BigDecimal totalAmount
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getStatus().name(),
            order.getTotal().amount()
        );
    }
}

// ============================================
// 2. Application Layer - 유스케이스 조율
// ============================================
@Service
@Transactional
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PaymentGateway paymentGateway;
    private final EventPublisher eventPublisher;

    public Result<Order> execute(Long customerId, List<OrderItemDto> itemDtos) {
        // 1. 고객 조회
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // 2. 상품 조회 및 재고 확인
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDto dto : itemDtos) {
            Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ProductNotFoundException(dto.productId()));

            if (!inventoryService.hasStock(product.getId(), dto.quantity())) {
                return Result.failure(
                    new Error("OUT_OF_STOCK", "재고 부족: " + product.getName())
                );
            }

            items.add(OrderItem.create(product, dto.quantity()));
        }

        // 3. 주문 생성 (도메인 로직)
        Result<Order> orderResult = Order.create(customer, items);
        if (orderResult.isFailure()) {
            return orderResult;
        }
        Order order = orderResult.getValue();

        // 4. 재고 차감
        for (OrderItem item : items) {
            inventoryService.decreaseStock(
                item.getProductId(),
                item.getQuantity()
            );
        }

        // 5. 결제 처리
        Result<Payment> paymentResult = paymentGateway.charge(
            customer.getPaymentMethod(),
            order.getTotal()
        );

        if (paymentResult.isFailure()) {
            // 재고 복구
            rollbackInventory(items);
            return Result.failure(paymentResult.getError());
        }

        // 6. 저장
        orderRepository.save(order);

        // 7. 이벤트 발행
        eventPublisher.publish(new OrderCreatedEvent(order.getId()));

        return Result.success(order);
    }

    private void rollbackInventory(List<OrderItem> items) {
        for (OrderItem item : items) {
            inventoryService.increaseStock(
                item.getProductId(),
                item.getQuantity()
            );
        }
    }
}

// ============================================
// 3. Domain Layer - 비즈니스 로직
// ============================================
public class Order {
    private Long id;
    private final Customer customer;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    private Order(Customer customer, List<OrderItem> items) {
        this.customer = customer;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // ✅ 도메인 로직 - 생성 검증
    public static Result<Order> create(Customer customer, List<OrderItem> items) {
        if (items.isEmpty()) {
            return Result.failure(
                new Error("EMPTY_ORDER", "주문 항목이 없습니다")
            );
        }

        if (items.size() > 100) {
            return Result.failure(
                new Error("TOO_MANY_ITEMS", "주문 항목은 100개 이하여야 합니다")
            );
        }

        Order order = new Order(customer, items);

        if (order.getTotal().amount().compareTo(new BigDecimal("10000000")) > 0) {
            return Result.failure(
                new Error("AMOUNT_EXCEEDED", "주문 금액이 한도를 초과했습니다")
            );
        }

        return Result.success(order);
    }

    // ✅ 도메인 로직 - 계산
    public Money getTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    // ✅ 도메인 로직 - 상태 변경
    public Result<Void> confirm() {
        if (status != OrderStatus.PENDING) {
            return Result.failure(
                new Error("INVALID_STATE", "확인할 수 없는 상태입니다")
            );
        }
        this.status = OrderStatus.CONFIRMED;
        return Result.success(null);
    }

    public Result<Void> cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            return Result.failure(
                new Error("CANNOT_CANCEL", "배송 중이거나 배송 완료된 주문은 취소할 수 없습니다")
            );
        }
        this.status = OrderStatus.CANCELLED;
        return Result.success(null);
    }

    // Getter만 (Setter 없음)
    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
}

// ============================================
// 4. Infrastructure Layer - 외부 시스템
// ============================================
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerId(Long customerId);
}

@Component
public class PaymentGatewayImpl implements PaymentGateway {
    private final RestTemplate restTemplate;

    @Override
    public Result<Payment> charge(PaymentMethod method, Money amount) {
        try {
            // 외부 결제 API 호출
            PaymentResponse response = restTemplate.postForObject(
                "https://payment-api.example.com/charge",
                new ChargeRequest(method, amount),
                PaymentResponse.class
            );

            if (response.isSuccess()) {
                return Result.success(new Payment(response.transactionId()));
            } else {
                return Result.failure(
                    new Error("PAYMENT_FAILED", response.errorMessage())
                );
            }
        } catch (Exception e) {
            return Result.failure(
                new Error("PAYMENT_ERROR", "결제 처리 중 오류 발생")
            );
        }
    }
}
```

### 레이어별 책임

| 레이어 | 책임 | 의존성 |
|--------|------|--------|
| **API** | HTTP 변환, 입력 검증 | Application |
| **Application** | 유스케이스 조율, 트랜잭션 | Domain, Infrastructure |
| **Domain** | 비즈니스 로직, 도메인 규칙 | 없음 (순수) |
| **Infrastructure** | 외부 시스템 연동 | Domain |

**핵심 원칙:**
- Domain은 다른 레이어에 의존하지 않음
- 의존성 방향: API → Application → Domain ← Infrastructure

---

## 도메인 모델 vs Entity vs DTO

### 혼란스러운 용어들

```
도메인 모델 (Domain Model)    ← 비즈니스 로직 포함
JPA Entity                   ← 데이터베이스 매핑
DTO (Data Transfer Object)   ← 계층 간 데이터 전송
```

### 실전 구분

```java
// ============================================
// 1. 도메인 모델 - 비즈니스 로직
// ============================================
public class Order {
    private Long id;
    private Customer customer;
    private List<OrderItem> items;
    private OrderStatus status;

    // ✅ 비즈니스 로직
    public Money getTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    public Result<Void> cancel() {
        // 취소 가능 여부 검증
        if (status == OrderStatus.SHIPPED) {
            return Result.failure(new Error("CANNOT_CANCEL", "배송 중"));
        }
        this.status = OrderStatus.CANCELLED;
        return Result.success(null);
    }
}

// ============================================
// 2. JPA Entity - DB 매핑
// ============================================
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;

    // ❌ 비즈니스 로직 없음 (단순 데이터 컨테이너)

    // Getter/Setter (JPA용)
}

// ============================================
// 3. DTO - 계층 간 데이터 전송
// ============================================
// API 요청
public record CreateOrderRequest(
    Long customerId,
    List<OrderItemRequest> items
) {}

// API 응답
public record OrderResponse(
    Long id,
    String customerName,
    String status,
    BigDecimal total,
    LocalDateTime createdAt
) {}

// 내부 전송용
public record OrderSummaryDto(
    Long orderId,
    String status,
    int itemCount
) {}
```

### 변환 흐름

```
API Request DTO
      ↓
  도메인 모델 (비즈니스 로직 실행)
      ↓
  JPA Entity (DB 저장)
      ↓
  도메인 모델 (DB에서 로드)
      ↓
API Response DTO
```

### 실제 코드

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. DTO → 도메인 모델
        List<OrderItem> items = request.items().stream()
            .map(dto -> OrderItem.create(
                dto.productId(),
                dto.quantity(),
                Money.of(dto.price())
            ))
            .toList();

        Result<Order> orderResult = Order.create(
            request.customerId(),
            items
        );

        if (orderResult.isFailure()) {
            throw new BusinessException(orderResult.getError());
        }

        Order order = orderResult.getValue();

        // 2. 도메인 모델 → JPA Entity
        OrderEntity entity = OrderMapper.toEntity(order);

        // 3. DB 저장
        OrderEntity savedEntity = orderRepository.save(entity);

        // 4. JPA Entity → 도메인 모델
        Order savedOrder = OrderMapper.toDomain(savedEntity);

        // 5. 도메인 모델 → Response DTO
        return OrderResponse.from(savedOrder);
    }
}

// Mapper
public class OrderMapper {
    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setCustomerId(order.getCustomer().getId());
        entity.setStatus(order.getStatus());
        entity.setTotalAmount(order.getTotal().amount());
        // ...
        return entity;
    }

    public static Order toDomain(OrderEntity entity) {
        // Entity → Domain 변환
        return Order.restore(
            entity.getId(),
            entity.getCustomerId(),
            entity.getStatus(),
            // ...
        );
    }
}
```

### 언제 분리하고 언제 합칠까?

**분리해야 할 때:**
- ✅ API 스펙과 도메인 모델이 다를 때
- ✅ 여러 도메인을 조합한 응답이 필요할 때
- ✅ 보안상 일부 필드를 숨겨야 할 때

```java
// API는 간단하지만 내부는 복잡
public record UserResponse(
    Long id,
    String name,
    String email
) {
    public static UserResponse from(User user, Profile profile) {
        return new UserResponse(
            user.getId(),
            profile.getDisplayName(),  // User가 아닌 Profile에서
            user.getEmail()
        );
    }
}
```

**합쳐도 될 때:**
- ✅ 단순 CRUD
- ✅ 도메인과 API가 1:1 매핑
- ✅ 내부 시스템 (외부 노출 안 됨)

```java
// 간단한 경우 Record 하나로
public record ProductDto(
    Long id,
    String name,
    BigDecimal price
) {}
```

---

## 의존성 관리

### 의존성 주입 (Dependency Injection)

```java
// ❌ 안티패턴: 직접 생성
public class OrderService {
    private OrderRepository orderRepository = new OrderRepositoryImpl();
    private PaymentGateway paymentGateway = new StripePaymentGateway();

    // 테스트 불가능, 교체 불가능
}

// ✅ 해결책: 생성자 주입
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    // Spring이 자동 주입 (단일 생성자면 @Autowired 생략 가능)
    public OrderService(
            OrderRepository orderRepository,
            PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }
}
```

### 인터페이스 vs 구체 클래스

```java
// ❌ 불필요한 인터페이스
public interface UserService {
    User findById(Long id);
}

@Service
public class UserServiceImpl implements UserService {
    // 구현체 하나뿐인데 인터페이스?
}

// ✅ 구체 클래스로 충분
@Service
public class UserService {
    public User findById(Long id) {
        // ...
    }
}
```

**인터페이스가 필요한 경우:**

```java
// ✅ 여러 구현체
public interface PaymentGateway {
    Result<Payment> charge(Money amount);
}

@Component("stripe")
public class StripePaymentGateway implements PaymentGateway {
    // Stripe API 구현
}

@Component("toss")
public class TossPaymentGateway implements PaymentGateway {
    // Toss API 구현
}

// 설정으로 선택
@Service
public class PaymentService {
    private final PaymentGateway gateway;

    public PaymentService(@Qualifier("stripe") PaymentGateway gateway) {
        this.gateway = gateway;
    }
}
```

### 순환 의존성 해결

```java
// ❌ 순환 의존성
@Service
public class UserService {
    @Autowired
    private OrderService orderService;  // A → B
}

@Service
public class OrderService {
    @Autowired
    private UserService userService;   // B → A (순환!)
}

// ✅ 해결 1: 공통 의존성 추출
@Service
public class UserOrderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // 하나의 서비스로 통합
}

// ✅ 해결 2: 이벤트로 결합도 낮추기
@Service
public class UserService {
    private final EventPublisher eventPublisher;

    public void deleteUser(Long userId) {
        userRepository.delete(userId);
        eventPublisher.publish(new UserDeletedEvent(userId));
    }
}

@Service
public class OrderService {
    @EventListener
    public void handleUserDeleted(UserDeletedEvent event) {
        orderRepository.cancelOrdersByUserId(event.getUserId());
    }
}
```

---

## 패키지 구조

### 레이어별 구조 (Package by Layer)

```
com.example.shop
├── controller
│   ├── OrderController
│   └── UserController
├── service
│   ├── OrderService
│   └── UserService
├── repository
│   ├── OrderRepository
│   └── UserRepository
└── domain
    ├── Order
    └── User
```

**단점:**
- 관련된 파일들이 멀리 떨어져 있음
- 하나의 기능을 수정하려면 여러 패키지를 오가야 함

### 기능별 구조 (Package by Feature) - 추천!

```
com.example.shop
├── order
│   ├── OrderController
│   ├── OrderService
│   ├── OrderRepository
│   ├── Order (domain)
│   ├── OrderItem (domain)
│   └── dto
│       ├── CreateOrderRequest
│       └── OrderResponse
├── user
│   ├── UserController
│   ├── UserService
│   ├── UserRepository
│   ├── User (domain)
│   └── dto
│       └── UserResponse
└── payment
    ├── PaymentService
    ├── PaymentGateway
    └── Payment (domain)
```

**장점:**
- 관련된 코드가 한 곳에 모여있음
- 기능 단위로 이해하기 쉬움
- 모듈화하기 쉬움

### 대규모 프로젝트 구조

```
com.example.shop
├── order (주문 모듈)
│   ├── api          ← Controller, Request/Response DTO
│   ├── application  ← UseCase, Application Service
│   ├── domain       ← Entity, Domain Service, Value Object
│   └── infrastructure ← Repository, External API Client
│
├── payment (결제 모듈)
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
│
├── user (사용자 모듈)
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
│
└── common (공통)
    ├── exception
    ├── util
    └── config
```

### Go 스타일 구조 (심플한 프로젝트)

```
src/main/java/com/example/shop
├── order
│   ├── Order.java            (domain)
│   ├── OrderService.java     (business logic)
│   ├── OrderRepository.java  (data access)
│   └── OrderController.java  (api)
│
├── user
│   ├── User.java
│   ├── UserService.java
│   ├── UserRepository.java
│   └── UserController.java
│
└── payment
    ├── Payment.java
    ├── PaymentService.java
    └── PaymentGateway.java
```

**Go와 비교:**
```
shop/
├── order/
│   ├── order.go           (domain + logic)
│   ├── repository.go      (data access)
│   └── handler.go         (http handler)
│
└── user/
    ├── user.go
    ├── repository.go
    └── handler.go
```

---

## 언제 추상화할 것인가

### YAGNI (You Aren't Gonna Need It)

```java
// ❌ 과도한 추상화
public interface DataSource<T> {
    T fetch(Query query);
}

public interface QueryBuilder<T> {
    Query build(T criteria);
}

public interface ResultTransformer<T, R> {
    R transform(T input);
}

// 3개 인터페이스를 써서 단순 조회?
```

```java
// ✅ 필요한 만큼만
@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbc.query(sql, userRowMapper, id)
                   .stream()
                   .findFirst();
    }
}
```

### 추상화가 필요한 시점

**1. 실제로 여러 구현체가 필요할 때**

```java
// ✅ 결제 수단이 여러 개
public interface PaymentGateway {
    Result<Payment> charge(Money amount);
}

public class StripePaymentGateway implements PaymentGateway { }
public class TossPaymentGateway implements PaymentGateway { }
public class PayPalPaymentGateway implements PaymentGateway { }
```

**2. 테스트를 위한 Mock이 필요할 때**

```java
// ✅ 외부 API는 Mock으로 테스트
public interface EmailService {
    void send(Email email);
}

// 프로덕션
public class SmtpEmailService implements EmailService {
    // 실제 이메일 발송
}

// 테스트
public class MockEmailService implements EmailService {
    public List<Email> sentEmails = new ArrayList<>();

    @Override
    public void send(Email email) {
        sentEmails.add(email);  // 메모리에 저장만
    }
}
```

**3. 전략 패턴이 명확할 때**

```java
// ✅ 할인 정책
public interface DiscountPolicy {
    Money calculate(Order order);
}

public class PercentageDiscount implements DiscountPolicy {
    private final double rate;

    @Override
    public Money calculate(Order order) {
        return order.getTotal().multiply(rate);
    }
}

public class FixedAmountDiscount implements DiscountPolicy {
    private final Money amount;

    @Override
    public Money calculate(Order order) {
        return amount;
    }
}
```

### 단순함을 유지하는 원칙

1. **일단 구체 클래스로 시작**
   ```java
   // 처음엔 이것만
   public class OrderService {
       public void createOrder(...) { }
   }
   ```

2. **두 번째 구현체가 필요할 때 인터페이스 추출**
   ```java
   // 이제 추상화
   public interface OrderService {
       void createOrder(...);
   }

   public class StandardOrderService implements OrderService { }
   public class ExpressOrderService implements OrderService { }
   ```

3. **3번 반복되면 패턴 도입**
   ```java
   // 비슷한 코드가 3번 나오면 공통화
   public abstract class BaseOrderService {
       public final void createOrder(...) {
           validate();
           process();
           notify();
       }

       protected abstract void validate();
       protected abstract void process();
   }
   ```

---

## 요약

| 주제 | 핵심 원칙 |
|------|----------|
| **레이어 분리** | API → Application → Domain ← Infrastructure |
| **도메인/Entity/DTO** | 도메인=로직, Entity=DB, DTO=전송 |
| **의존성** | 생성자 주입, 필요할 때만 인터페이스 |
| **패키지 구조** | 기능별 구조 (Package by Feature) 추천 |
| **추상화** | YAGNI - 필요할 때만 |

**기억하세요:**
- 단순함에서 시작해서 필요할 때 복잡도를 추가
- 추상화는 공짜가 아니다 (유지보수 비용)
- 읽기 쉬운 코드가 좋은 코드

---

**이전**: ← [현대적 Java 기능 활용](./02-modern-java.md)
**다음**: [협업 Best Practices](./04-collaboration.md) →
