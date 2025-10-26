package com.example.presentation.api;

import com.example.application.*;
import com.example.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Controller Layer 예제
 *
 * 핵심 원칙:
 * 1. Result 패턴을 HTTP Response로 변환
 * 2. Switch expression으로 명시적 에러 처리
 * 3. UseCase에 위임 (비즈니스 로직 없음)
 * 4. Validation은 Bean Validation 사용
 */

// ============================================================================
// User Controller
// ============================================================================

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final LoginUseCase loginUseCase;

    // Constructor Injection
    public UserController(
        CreateUserUseCase createUserUseCase,
        GetUserUseCase getUserUseCase,
        LoginUseCase loginUseCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    /**
     * 사용자 생성
     *
     * Result 패턴을 사용한 명시적 에러 처리
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserApiRequest request) {
        // UseCase 실행
        Result<UserResponse, UserError> result = createUserUseCase.execute(
            new CreateUserRequest(
                request.username(),
                request.email(),
                request.password()
            )
        );

        // Switch expression으로 Result 처리
        return switch (result) {
            case Result.Success<UserResponse, UserError> s ->
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(s.value()));

            case Result.Failure<UserResponse, UserError> f -> switch (f.error()) {
                case UserError.EmailAlreadyExists e ->
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("EMAIL_EXISTS", e.message()));

                case UserError.InvalidInput e ->
                    ResponseEntity.badRequest()
                        .body(ApiResponse.error("INVALID_INPUT", e.message()));

                default ->
                    ResponseEntity.internalServerError()
                        .body(ApiResponse.error("INTERNAL_ERROR", "An error occurred"));
            };
        };
    }

    /**
     * 사용자 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Result<UserResponse, UserError> result = getUserUseCase.execute(id);

        return switch (result) {
            case Result.Success<UserResponse, UserError> s ->
                ResponseEntity.ok(ApiResponse.success(s.value()));

            case Result.Failure<UserResponse, UserError> f -> switch (f.error()) {
                case UserError.NotFound e ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("USER_NOT_FOUND", e.message()));

                default ->
                    ResponseEntity.internalServerError()
                        .body(ApiResponse.error("INTERNAL_ERROR", "An error occurred"));
            };
        };
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginApiRequest request) {
        Result<LoginResponse, UserError> result = loginUseCase.execute(
            new LoginRequest(request.email(), request.password())
        );

        return switch (result) {
            case Result.Success<LoginResponse, UserError> s ->
                ResponseEntity.ok(ApiResponse.success(s.value()));

            case Result.Failure<LoginResponse, UserError> f -> switch (f.error()) {
                case UserError.NotFound e ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("INVALID_CREDENTIALS", "Invalid email or password"));

                case UserError.InvalidPassword e ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("INVALID_CREDENTIALS", "Invalid email or password"));

                case UserError.AccountLocked e ->
                    ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("ACCOUNT_LOCKED", e.message()));

                default ->
                    ResponseEntity.internalServerError()
                        .body(ApiResponse.error("INTERNAL_ERROR", "An error occurred"));
            };
        };
    }
}

// ============================================================================
// Order Controller
// ============================================================================

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetUserOrdersUseCase getUserOrdersUseCase;

    public OrderController(
        PlaceOrderUseCase placeOrderUseCase,
        GetOrderUseCase getOrderUseCase,
        CancelOrderUseCase cancelOrderUseCase,
        GetUserOrdersUseCase getUserOrdersUseCase
    ) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getUserOrdersUseCase = getUserOrdersUseCase;
    }

    /**
     * 주문 생성
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody PlaceOrderApiRequest request) {
        Result<OrderResponse, OrderError> result = placeOrderUseCase.execute(
            new PlaceOrderRequest(
                request.userId(),
                request.items().stream()
                    .map(item -> new OrderItemRequest(
                        item.productId(),
                        item.productName(),
                        item.unitPrice(),
                        item.quantity()
                    ))
                    .toList(),
                request.paymentMethod()
            )
        );

        return switch (result) {
            case Result.Success<OrderResponse, OrderError> s ->
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(s.value()));

            case Result.Failure<OrderResponse, OrderError> f -> switch (f.error()) {
                case OrderError.InvalidInput e ->
                    ResponseEntity.badRequest()
                        .body(ApiResponse.error("INVALID_INPUT", e.message()));

                default ->
                    ResponseEntity.internalServerError()
                        .body(ApiResponse.error("INTERNAL_ERROR", "An error occurred"));
            };
        };
    }

    /**
     * 주문 조회
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<?> getOrder(@PathVariable String orderNumber) {
        Result<OrderResponse, OrderError> result = getOrderUseCase.execute(orderNumber);

        return switch (result) {
            case Result.Success<OrderResponse, OrderError> s ->
                ResponseEntity.ok(ApiResponse.success(s.value()));

            case Result.Failure<OrderResponse, OrderError> f -> switch (f.error()) {
                case OrderError.NotFound e ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("ORDER_NOT_FOUND", e.message()));

                default ->
                    ResponseEntity.internalServerError()
                        .body(ApiResponse.error("INTERNAL_ERROR", "An error occurred"));
            };
        };
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderNumber) {
        Result<Void, OrderError> result = cancelOrderUseCase.execute(orderNumber);

        return switch (result) {
            case Result.Success<Void, OrderError> s ->
                ResponseEntity.ok(ApiResponse.success("Order cancelled successfully"));

            case Result.Failure<Void, OrderError> f -> switch (f.error()) {
                case OrderError.NotFound e ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("ORDER_NOT_FOUND", e.message()));

                case OrderError.CannotCancelOrder e ->
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("CANNOT_CANCEL", e.message()));

                case OrderError.AlreadyCancelled e ->
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("ALREADY_CANCELLED", e.message()));

                default ->
                    ResponseEntity.internalServerError()
                        .body(ApiResponse.error("INTERNAL_ERROR", "An error occurred"));
            };
        };
    }

    /**
     * 사용자의 주문 목록 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        Result<List<OrderResponse>, OrderError> result = getUserOrdersUseCase.execute(userId);

        return switch (result) {
            case Result.Success<List<OrderResponse>, OrderError> s ->
                ResponseEntity.ok(ApiResponse.success(s.value()));

            case Result.Failure<List<OrderResponse>, OrderError> f ->
                ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "Failed to fetch orders"));
        };
    }
}

// ============================================================================
// API Request DTOs (Bean Validation 적용)
// ============================================================================

/**
 * API 요청 DTO
 * - Bean Validation으로 입력 검증
 * - Record 사용으로 불변성 보장
 */
public record CreateUserApiRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {}

public record LoginApiRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {}

public record PlaceOrderApiRequest(
    @NotNull(message = "User ID is required")
    Long userId,

    @NotEmpty(message = "Order must have at least one item")
    List<@Valid OrderItemApiRequest> items,

    @NotBlank(message = "Payment method is required")
    String paymentMethod
) {}

public record OrderItemApiRequest(
    @NotBlank(message = "Product ID is required")
    String productId,

    @NotBlank(message = "Product name is required")
    String productName,

    @Positive(message = "Unit price must be positive")
    double unitPrice,

    @Positive(message = "Quantity must be positive")
    int quantity
) {}

// ============================================================================
// API Response Wrapper
// ============================================================================

/**
 * 일관된 API 응답 형식
 */
public record ApiResponse<T>(
    boolean success,
    T data,
    ErrorDetails error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorDetails(code, message));
    }

    public record ErrorDetails(String code, String message) {}
}

// ============================================================================
// Global Exception Handler
// ============================================================================

/**
 * 전역 예외 처리
 *
 * 비즈니스 에러는 Result로 처리하고,
 * 시스템 예외만 여기서 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation 에러 처리
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
        org.springframework.web.bind.MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Validation failed");

        return ResponseEntity.badRequest()
            .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        // 로깅
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();

        return ResponseEntity.internalServerError()
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    /**
     * IllegalArgumentException 처리 (Value Object 생성 실패 등)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("INVALID_ARGUMENT", e.getMessage()));
    }
}

// ============================================================================
// Additional UseCases (for completeness)
// ============================================================================

/**
 * 추가 UseCase 인터페이스들
 */
interface GetUserUseCase {
    Result<UserResponse, UserError> execute(Long userId);
}

interface LoginUseCase {
    Result<LoginResponse, UserError> execute(LoginRequest request);
}

interface GetOrderUseCase {
    Result<OrderResponse, OrderError> execute(String orderNumber);
}

interface GetUserOrdersUseCase {
    Result<List<OrderResponse>, OrderError> execute(Long userId);
}

record LoginRequest(String email, String password) {}

record LoginResponse(
    Long userId,
    String username,
    String email,
    String token,  // JWT token
    long expiresIn
) {}

// ============================================================================
// Golang과의 비교
// ============================================================================

/**
 * Golang의 핸들러와 비교:
 *
 * // Golang
 * func CreateUser(w http.ResponseWriter, r *http.Request) {
 *     var req CreateUserRequest
 *     if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
 *         respondError(w, http.StatusBadRequest, err)
 *         return
 *     }
 *
 *     user, err := userService.Create(req.Username, req.Email, req.Password)
 *     if err != nil {
 *         switch err {
 *         case ErrEmailExists:
 *             respondError(w, http.StatusConflict, err)
 *         case ErrInvalidInput:
 *             respondError(w, http.StatusBadRequest, err)
 *         default:
 *             respondError(w, http.StatusInternalServerError, err)
 *         }
 *         return
 *     }
 *
 *     respondJSON(w, http.StatusCreated, user)
 * }
 *
 * Java의 장점:
 * 1. Spring의 자동 JSON 변환
 * 2. Bean Validation으로 자동 검증
 * 3. Switch expression으로 타입 안전한 에러 처리
 * 4. 컴파일 타임에 모든 케이스 처리 강제
 *
 * Golang의 장점:
 * 1. 더 간결한 코드
 * 2. 명시적인 에러 처리 (if err != nil)
 * 3. 컴파일 속도 빠름
 *
 * 융합:
 * - Golang의 명시적 에러 처리 (error as value) + Java의 타입 안전성
 * - Result<T, E> 패턴으로 두 장점 모두 활용
 */

// ============================================================================
// 핵심 정리
// ============================================================================

/**
 * Controller Layer 설계 원칙:
 *
 * 1. 비즈니스 로직 없음
 *    - Controller는 HTTP 요청/응답 변환만 담당
 *    - 모든 로직은 UseCase에 위임
 *
 * 2. Result 패턴을 HTTP Response로 변환
 *    - Switch expression으로 명시적 변환
 *    - 각 에러 타입마다 적절한 HTTP 상태 코드
 *
 * 3. Bean Validation 활용
 *    - @Valid로 입력 검증 자동화
 *    - MethodArgumentNotValidException 처리
 *
 * 4. 일관된 API 응답 형식
 *    - ApiResponse로 래핑
 *    - success/error 구조 통일
 *
 * 5. Global Exception Handler
 *    - 시스템 예외만 처리
 *    - 비즈니스 에러는 Result로
 *
 * 6. RESTful 설계
 *    - 적절한 HTTP 메서드 사용
 *    - 의미있는 상태 코드 반환
 */
