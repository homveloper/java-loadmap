package practical.guide.examples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 리팩토링 후 - 모범 사례
 *
 * 개선점:
 * 1. Rich Domain Model - 비즈니스 로직이 도메인에
 * 2. 불변성 - final 필드, setter 없음
 * 3. Enum으로 매직 넘버 제거
 * 4. Early Return으로 중첩 제거
 * 5. Result 패턴으로 명시적 에러 처리
 */

// ✅ Enum으로 상태 관리
enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

// ✅ 값 객체 (Value Object)
record Money(BigDecimal amount) {
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money add(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money multiply(int quantity) {
        return new Money(amount.multiply(new BigDecimal(quantity)));
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }
}

// ✅ Rich Domain Model
class Order {
    private static final Money MAX_ORDER_AMOUNT = new Money(new BigDecimal("10000000"));

    private final Long id;
    private final Long customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    // private 생성자
    private Order(Long id, Long customerId, List<OrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // ✅ 정적 팩토리 메서드 with 검증
    public static Result<Order> create(Long customerId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Result.failure(Error.validation("주문 항목이 없습니다"));
        }

        Order order = new Order(null, customerId, items);

        Money total = order.calculateTotal();
        if (total.isGreaterThan(MAX_ORDER_AMOUNT)) {
            return Result.failure(Error.validation("주문 금액이 한도를 초과했습니다"));
        }

        return Result.success(order);
    }

    // ✅ 도메인 로직 - 계산
    public Money calculateTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    // ✅ 도메인 로직 - 상태 변경
    public Result<Void> cancel() {
        if (!canCancel()) {
            return Result.failure(
                Error.of("CANNOT_CANCEL", "취소할 수 없는 상태입니다: " + status)
            );
        }

        this.status = OrderStatus.CANCELLED;
        return Result.success(null);
    }

    public Result<Void> confirm() {
        if (status != OrderStatus.PENDING) {
            return Result.failure(
                Error.of("CANNOT_CONFIRM", "확인할 수 없는 상태입니다: " + status)
            );
        }

        this.status = OrderStatus.CONFIRMED;
        return Result.success(null);
    }

    // ✅ 도메인 규칙
    private boolean canCancel() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    // Getter만 (Setter 없음)
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

// ✅ 값 객체
class OrderItem {
    private final Long productId;
    private final int quantity;
    private final Money price;

    private OrderItem(Long productId, int quantity, Money price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public static Result<OrderItem> create(Long productId, int quantity, Money price) {
        if (quantity <= 0) {
            return Result.failure(Error.validation("수량은 1 이상이어야 합니다"));
        }

        if (price.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(Error.validation("가격은 0보다 커야 합니다"));
        }

        return Result.success(new OrderItem(productId, quantity, price));
    }

    public Money getSubtotal() {
        return price.multiply(quantity);
    }

    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public Money getPrice() { return price; }
}

// ✅ 서비스는 조율만 (도메인 로직 X)
class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InventoryService inventoryService;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.inventoryService = inventoryService;
    }

    // ✅ Early Return으로 중첩 제거
    public Result<Order> createOrder(Long customerId, List<CreateOrderItemDto> itemDtos) {
        // 1. 고객 확인
        Customer customer = customerRepository.findById(customerId)
            .orElse(null);

        if (customer == null) {
            return Result.failure(Error.notFound("고객을 찾을 수 없습니다"));
        }

        if (!customer.isActive()) {
            return Result.failure(Error.validation("비활성 고객입니다"));
        }

        // 2. 주문 항목 생성 및 검증
        List<OrderItem> items = new ArrayList<>();
        for (CreateOrderItemDto dto : itemDtos) {
            Product product = productRepository.findById(dto.productId())
                .orElse(null);

            if (product == null) {
                return Result.failure(Error.notFound("상품을 찾을 수 없습니다: " + dto.productId()));
            }

            if (!inventoryService.hasStock(product.getId(), dto.quantity())) {
                return Result.failure(Error.validation("재고 부족: " + product.getName()));
            }

            Result<OrderItem> itemResult = OrderItem.create(
                product.getId(),
                dto.quantity(),
                new Money(product.getPrice())
            );

            if (itemResult.isFailure()) {
                return Result.failure(itemResult.getError());
            }

            items.add(itemResult.getValue());
        }

        // 3. 주문 생성 (도메인 로직)
        Result<Order> orderResult = Order.create(customerId, items);
        if (orderResult.isFailure()) {
            return orderResult;
        }

        Order order = orderResult.getValue();

        // 4. 재고 차감
        for (OrderItem item : items) {
            inventoryService.decreaseStock(item.getProductId(), item.getQuantity());
        }

        // 5. 저장
        orderRepository.save(order);

        return Result.success(order);
    }

    // ✅ 간단한 조율
    public Result<Void> cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElse(null);

        if (order == null) {
            return Result.failure(Error.notFound("주문을 찾을 수 없습니다"));
        }

        // 도메인 로직에 위임
        Result<Void> cancelResult = order.cancel();
        if (cancelResult.isFailure()) {
            return cancelResult;
        }

        orderRepository.save(order);
        return Result.success(null);
    }
}

// DTO
record CreateOrderItemDto(Long productId, int quantity) {}

// Dummy classes
class Customer {
    public boolean isActive() { return true; }
}

class Product {
    private Long id;
    private String name;
    private BigDecimal price;

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
}

interface OrderRepository {
    void save(Order order);
    java.util.Optional<Order> findById(Long id);
}

interface ProductRepository {
    java.util.Optional<Product> findById(Long id);
}

interface CustomerRepository {
    java.util.Optional<Customer> findById(Long id);
}

interface InventoryService {
    boolean hasStock(Long productId, int quantity);
    void decreaseStock(Long productId, int quantity);
}

public class AfterRefactoring {
    // 리팩토링 후 모범 사례
}
