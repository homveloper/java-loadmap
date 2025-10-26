package com.example.domain.model;

import com.example.common.Result;
import com.example.domain.valueobject.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Rich Domain Model 예제
 *
 * 핵심 원칙:
 * 1. 비즈니스 로직을 도메인 모델에 위치
 * 2. Static Factory Method로 객체 생성
 * 3. 불변성 최대한 보장
 * 4. Result 패턴으로 에러 처리
 */

// ============================================================================
// User Domain Model
// ============================================================================

public class User {

    // 불변 필드
    private final UserId id;
    private final Email email;

    // 가변 필드 (비즈니스 규칙에 따라 변경 가능)
    private String username;
    private String hashedPassword;
    private UserStatus status;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor - 직접 생성 방지
    private User(
        UserId id,
        String username,
        Email email,
        String hashedPassword,
        UserStatus status,
        int failedLoginAttempts,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.status = status;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========================================================================
    // Static Factory Methods
    // ========================================================================

    /**
     * 새로운 사용자 생성 (회원가입)
     */
    public static Result<User, UserError> create(
        String username,
        String email,
        String rawPassword
    ) {
        // 유효성 검증
        if (username == null || username.isBlank()) {
            return Result.failure(new UserError.InvalidInput("Username is required"));
        }

        if (username.length() < 3 || username.length() > 20) {
            return Result.failure(new UserError.InvalidInput(
                "Username must be between 3 and 20 characters"
            ));
        }

        if (rawPassword == null || rawPassword.length() < 8) {
            return Result.failure(new UserError.InvalidInput(
                "Password must be at least 8 characters"
            ));
        }

        // Email은 Value Object에서 유효성 검증
        Email validEmail;
        try {
            validEmail = Email.of(email);
        } catch (IllegalArgumentException e) {
            return Result.failure(new UserError.InvalidInput("Invalid email: " + e.getMessage()));
        }

        // 비밀번호 해싱 (실제로는 BCrypt 등 사용)
        String hashed = hashPassword(rawPassword);

        LocalDateTime now = LocalDateTime.now();

        User user = new User(
            null,  // ID는 저장 시 생성
            username,
            validEmail,
            hashed,
            UserStatus.ACTIVE,
            0,
            null,
            now,
            now
        );

        return Result.success(user);
    }

    /**
     * 비활성 사용자 생성 (관리자가 임시 계정 생성)
     */
    public static User createInactive(String username, Email email) {
        LocalDateTime now = LocalDateTime.now();
        return new User(
            null,
            username,
            email,
            null,  // 비밀번호 미설정
            UserStatus.INACTIVE,
            0,
            null,
            now,
            now
        );
    }

    /**
     * DB로부터 복원 (Repository에서 사용)
     */
    public static User restore(
        UserId id,
        String username,
        Email email,
        String hashedPassword,
        UserStatus status,
        int failedLoginAttempts,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new User(
            id,
            username,
            email,
            hashedPassword,
            status,
            failedLoginAttempts,
            lastLoginAt,
            createdAt,
            updatedAt
        );
    }

    // ========================================================================
    // 비즈니스 로직 (Rich Domain Model)
    // ========================================================================

    /**
     * 사용자 활성화
     */
    public Result<Void, UserError> activate() {
        if (this.status == UserStatus.BANNED) {
            return Result.failure(new UserError.OperationNotAllowed(
                "Cannot activate banned user: " + this.id
            ));
        }

        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.updatedAt = LocalDateTime.now();

        return Result.successVoid();
    }

    /**
     * 로그인 성공 처리
     */
    public Result<Void, UserError> recordSuccessfulLogin() {
        if (this.status == UserStatus.BANNED) {
            return Result.failure(new UserError.OperationNotAllowed(
                "Banned user cannot login: " + this.id
            ));
        }

        if (this.status == UserStatus.LOCKED) {
            return Result.failure(new UserError.AccountLocked(
                "Account is locked due to too many failed login attempts: " + this.id
            ));
        }

        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();

        return Result.successVoid();
    }

    /**
     * 로그인 실패 처리
     */
    public Result<Void, UserError> recordFailedLogin() {
        this.failedLoginAttempts++;
        this.updatedAt = LocalDateTime.now();

        // 5회 실패 시 계정 잠김
        if (this.failedLoginAttempts >= 5) {
            this.status = UserStatus.LOCKED;
            return Result.failure(new UserError.AccountLocked(
                "Account locked after 5 failed login attempts: " + this.id
            ));
        }

        return Result.successVoid();
    }

    /**
     * 비밀번호 변경
     */
    public Result<Void, UserError> changePassword(
        String oldPassword,
        String newPassword
    ) {
        if (!verifyPassword(oldPassword)) {
            return Result.failure(new UserError.InvalidPassword(
                "Old password is incorrect"
            ));
        }

        if (newPassword == null || newPassword.length() < 8) {
            return Result.failure(new UserError.InvalidInput(
                "New password must be at least 8 characters"
            ));
        }

        this.hashedPassword = hashPassword(newPassword);
        this.updatedAt = LocalDateTime.now();

        return Result.successVoid();
    }

    /**
     * 사용자 정지
     */
    public Result<Void, UserError> ban(String reason) {
        if (this.status == UserStatus.BANNED) {
            return Result.failure(new UserError.OperationNotAllowed(
                "User is already banned: " + this.id
            ));
        }

        this.status = UserStatus.BANNED;
        this.updatedAt = LocalDateTime.now();

        return Result.successVoid();
    }

    // ========================================================================
    // 비즈니스 규칙 검증
    // ========================================================================

    public boolean canLogin() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean canPlaceOrder() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isLocked() {
        return this.status == UserStatus.LOCKED;
    }

    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    private static String hashPassword(String rawPassword) {
        // 실제로는 BCrypt, Argon2 등 사용
        return "hashed_" + rawPassword;
    }

    private boolean verifyPassword(String rawPassword) {
        // 실제로는 BCrypt.checkpw() 등 사용
        return this.hashedPassword.equals(hashPassword(rawPassword));
    }

    // ========================================================================
    // Getters (Immutable fields는 setter 없음)
    // ========================================================================

    public UserId getId() { return id; }
    public String getUsername() { return username; }
    public Email getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // hashedPassword는 외부 노출 금지
    String getHashedPassword() { return hashedPassword; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

// ============================================================================
// Order Domain Model
// ============================================================================

public class Order {

    private final OrderNumber orderNumber;
    private final UserId userId;
    private final List<OrderItem> items;

    private OrderStatus status;
    private Money totalAmount;
    private LocalDateTime orderedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;

    // Private constructor
    private Order(
        OrderNumber orderNumber,
        UserId userId,
        List<OrderItem> items,
        OrderStatus status,
        Money totalAmount,
        LocalDateTime orderedAt,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt
    ) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.items = new ArrayList<>(items);  // 방어적 복사
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderedAt = orderedAt;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
    }

    // ========================================================================
    // Static Factory Methods
    // ========================================================================

    /**
     * 새로운 주문 생성
     */
    public static Result<Order, OrderError> create(
        UserId userId,
        List<OrderItem> items,
        int orderSequence
    ) {
        if (userId == null) {
            return Result.failure(new OrderError.InvalidInput("User ID is required"));
        }

        if (items == null || items.isEmpty()) {
            return Result.failure(new OrderError.InvalidInput("Order must have at least one item"));
        }

        // 주문 금액 계산
        Money total = items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(Money.ZERO_KRW, Money::add);

        if (total.isZero() || total.isNegative()) {
            return Result.failure(new OrderError.InvalidInput("Order total must be positive"));
        }

        Order order = new Order(
            OrderNumber.generate(orderSequence),
            userId,
            items,
            OrderStatus.PENDING,
            total,
            LocalDateTime.now(),
            null,
            null
        );

        return Result.success(order);
    }

    /**
     * DB로부터 복원
     */
    public static Order restore(
        OrderNumber orderNumber,
        UserId userId,
        List<OrderItem> items,
        OrderStatus status,
        Money totalAmount,
        LocalDateTime orderedAt,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt
    ) {
        return new Order(
            orderNumber,
            userId,
            items,
            status,
            totalAmount,
            orderedAt,
            confirmedAt,
            cancelledAt
        );
    }

    // ========================================================================
    // 비즈니스 로직
    // ========================================================================

    /**
     * 주문 확인
     */
    public Result<Void, OrderError> confirm() {
        if (this.status != OrderStatus.PENDING) {
            return Result.failure(new OrderError.InvalidStatusTransition(
                "Cannot confirm order in status: " + this.status
            ));
        }

        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();

        return Result.successVoid();
    }

    /**
     * 주문 취소
     */
    public Result<Void, OrderError> cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            return Result.failure(new OrderError.AlreadyCancelled(this.orderNumber));
        }

        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            return Result.failure(new OrderError.CannotCancelOrder(
                "Cannot cancel order that is already shipped or delivered: " + this.orderNumber
            ));
        }

        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();

        return Result.successVoid();
    }

    /**
     * 주문 항목 추가 (PENDING 상태에서만 가능)
     */
    public Result<Void, OrderError> addItem(OrderItem item) {
        if (this.status != OrderStatus.PENDING) {
            return Result.failure(new OrderError.InvalidStatusTransition(
                "Cannot modify order in status: " + this.status
            ));
        }

        this.items.add(item);
        recalculateTotal();

        return Result.successVoid();
    }

    /**
     * 주문 항목 제거
     */
    public Result<Void, OrderError> removeItem(OrderItem item) {
        if (this.status != OrderStatus.PENDING) {
            return Result.failure(new OrderError.InvalidStatusTransition(
                "Cannot modify order in status: " + this.status
            ));
        }

        if (this.items.size() == 1) {
            return Result.failure(new OrderError.InvalidInput(
                "Cannot remove the last item from order"
            ));
        }

        this.items.remove(item);
        recalculateTotal();

        return Result.successVoid();
    }

    /**
     * 할인 적용
     */
    public Result<Void, OrderError> applyDiscount(Percentage discountRate) {
        if (this.status != OrderStatus.PENDING) {
            return Result.failure(new OrderError.InvalidStatusTransition(
                "Cannot apply discount to order in status: " + this.status
            ));
        }

        Money discountAmount = discountRate.applyTo(this.totalAmount);
        this.totalAmount = this.totalAmount.subtract(discountAmount);

        return Result.successVoid();
    }

    // ========================================================================
    // 비즈니스 규칙 검증
    // ========================================================================

    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING
            || this.status == OrderStatus.CONFIRMED;
    }

    public boolean canBeModified() {
        return this.status == OrderStatus.PENDING;
    }

    public boolean isCompleted() {
        return this.status == OrderStatus.DELIVERED;
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(Money.ZERO_KRW, Money::add);
    }

    // ========================================================================
    // Getters
    // ========================================================================

    public OrderNumber getOrderNumber() { return orderNumber; }
    public UserId getUserId() { return userId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public OrderStatus getStatus() { return status; }
    public Money getTotalAmount() { return totalAmount; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderNumber, order.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber);
    }
}

// ============================================================================
// OrderItem Value Object (Order의 일부)
// ============================================================================

public record OrderItem(
    String productId,
    String productName,
    Money unitPrice,
    int quantity
) {
    public OrderItem {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        Objects.requireNonNull(productName, "Product name cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(quantity);
    }
}

// ============================================================================
// Enums
// ============================================================================

enum UserStatus {
    ACTIVE,      // 활성
    INACTIVE,    // 비활성
    LOCKED,      // 잠김 (로그인 실패 5회)
    BANNED       // 정지
}

enum OrderStatus {
    PENDING,     // 대기
    CONFIRMED,   // 확인됨
    PREPARING,   // 준비 중
    SHIPPED,     // 배송 중
    DELIVERED,   // 배송 완료
    CANCELLED    // 취소됨
}

// ============================================================================
// Error Types
// ============================================================================

sealed interface UserError permits
    UserError.NotFound,
    UserError.InvalidInput,
    UserError.InvalidPassword,
    UserError.AccountLocked,
    UserError.OperationNotAllowed,
    UserError.EmailAlreadyExists {

    record NotFound(UserId userId) implements UserError {
        public String message() {
            return "User not found: " + userId;
        }
    }

    record InvalidInput(String reason) implements UserError {
        public String message() {
            return "Invalid input: " + reason;
        }
    }

    record InvalidPassword(String reason) implements UserError {
        public String message() {
            return "Invalid password: " + reason;
        }
    }

    record AccountLocked(String reason) implements UserError {
        public String message() {
            return "Account locked: " + reason;
        }
    }

    record OperationNotAllowed(String reason) implements UserError {
        public String message() {
            return "Operation not allowed: " + reason;
        }
    }

    record EmailAlreadyExists(Email email) implements UserError {
        public String message() {
            return "Email already exists: " + email;
        }
    }
}

sealed interface OrderError permits
    OrderError.NotFound,
    OrderError.InvalidInput,
    OrderError.InvalidStatusTransition,
    OrderError.CannotCancelOrder,
    OrderError.AlreadyCancelled {

    record NotFound(OrderNumber orderNumber) implements OrderError {
        public String message() {
            return "Order not found: " + orderNumber;
        }
    }

    record InvalidInput(String reason) implements OrderError {
        public String message() {
            return "Invalid input: " + reason;
        }
    }

    record InvalidStatusTransition(String reason) implements OrderError {
        public String message() {
            return "Invalid status transition: " + reason;
        }
    }

    record CannotCancelOrder(String reason) implements OrderError {
        public String message() {
            return "Cannot cancel order: " + reason;
        }
    }

    record AlreadyCancelled(OrderNumber orderNumber) implements OrderError {
        public String message() {
            return "Order already cancelled: " + orderNumber;
        }
    }
}
