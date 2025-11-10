package practical.guide.examples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 리팩토링 전 - 안티패턴 예시
 *
 * 문제점:
 * 1. God Class - 너무 많은 책임
 * 2. Anemic Domain Model - 비즈니스 로직이 서비스에
 * 3. 의미없는 getter/setter
 * 4. 과도한 중첩
 * 5. 매직 넘버
 */

// ❌ 안티패턴: 모든 게 public이고 setter 존재
class Order {
    private Long id;
    private Long customerId;
    private List<OrderItem> items;
    private int status;  // 0=pending, 1=confirmed, 2=shipped (매직 넘버!)
    private BigDecimal total;
    private LocalDateTime createdAt;

    // getter/setter 잔뜩...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

class OrderItem {
    private Long productId;
    private int quantity;
    private BigDecimal price;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

// ❌ God Class: 모든 비즈니스 로직이 서비스에
class OrderService {
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private CustomerRepository customerRepository;

    // ❌ 너무 많은 일을 한 메서드에서 처리
    public void createOrder(Long customerId, List<OrderItem> items) {
        // 중첩 지옥
        if (customerId != null) {
            Customer c = customerRepository.findById(customerId);
            if (c != null) {
                if (c.getStatus() == 1) {  // 매직 넘버
                    if (items != null && !items.isEmpty()) {
                        BigDecimal total = BigDecimal.ZERO;
                        for (OrderItem item : items) {
                            Product p = productRepository.findById(item.getProductId());
                            if (p != null) {
                                if (p.getStock() >= item.getQuantity()) {
                                    total = total.add(
                                        p.getPrice().multiply(new BigDecimal(item.getQuantity()))
                                    );
                                } else {
                                    throw new RuntimeException("재고 부족");
                                }
                            }
                        }

                        // 매직 넘버로 비즈니스 규칙
                        if (total.compareTo(new BigDecimal("10000000")) > 0) {
                            throw new RuntimeException("주문 금액 초과");
                        }

                        Order order = new Order();
                        order.setCustomerId(customerId);
                        order.setItems(items);
                        order.setTotal(total);
                        order.setStatus(0);  // pending
                        order.setCreatedAt(LocalDateTime.now());

                        orderRepository.save(order);
                    }
                }
            }
        }
    }

    // ❌ 총액 계산이 서비스에 (도메인 로직인데)
    public BigDecimal calculateTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            Product p = productRepository.findById(item.getProductId());
            total = total.add(p.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

    // ❌ 상태 변경 검증 로직이 서비스에
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId);
        if (order != null) {
            if (order.getStatus() == 0 || order.getStatus() == 1) {  // pending or confirmed
                order.setStatus(3);  // cancelled
                orderRepository.save(order);
            } else {
                throw new RuntimeException("취소할 수 없는 상태");
            }
        }
    }
}

// Dummy classes
class Customer {
    private int status;
    public int getStatus() { return status; }
}

class Product {
    private BigDecimal price;
    private int stock;
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
}

interface OrderRepository {
    void save(Order order);
    Order findById(Long id);
}

interface ProductRepository {
    Product findById(Long id);
}

interface CustomerRepository {
    Customer findById(Long id);
}

public class BeforeRefactoring {
    // 이 클래스는 안티패턴 예시 모음입니다
}
