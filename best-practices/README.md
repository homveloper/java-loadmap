# Java 백엔드 웹 개발 Best Practices
## Golang과 Java 철학의 융합

> **목표**: Golang의 실용적이고 간결한 철학과 Java의 강력한 객체지향 설계를 결합하여 프로덕션 레벨의 견고한 백엔드 시스템 구축

---

## 📋 목차

1. [핵심 철학](#핵심-철학)
2. [Error as Value - 에러 처리 패턴](#1-error-as-value---에러-처리-패턴)
3. [Composition over Inheritance - 컴포지션 우선](#2-composition-over-inheritance---컴포지션-우선)
4. [Static Factory Methods - 정적 팩토리 메서드](#3-static-factory-methods---정적-팩토리-메서드)
5. [Dependency Injection & IoC - 의존성 관리](#4-dependency-injection--ioc---의존성-관리)
6. [Separation of Concerns - 책임 분리](#5-separation-of-concerns---책임-분리)
7. [Anti-Patterns - 피해야 할 패턴](#6-anti-patterns---피해야-할-패턴)
8. [실전 적용 가이드](#7-실전-적용-가이드)

---

## 핵심 철학

### Golang의 장점
- **명시적 에러 처리**: Exception throwing 대신 error as value
- **간결함**: 최소한의 문법으로 명확한 의도 표현
- **컴포지션**: 구조체 임베딩을 통한 유연한 설계
- **인터페이스**: 작고 명확한 인터페이스 (implicit implementation)

### Java의 장점
- **강력한 타입 시스템**: 컴파일 타임 안정성
- **성숙한 생태계**: Spring Framework, JPA 등 검증된 도구들
- **엔터프라이즈 패턴**: 의존성 주입, AOP 등 대규모 시스템 설계 패턴

### 융합 전략
두 언어의 장점을 결합하여 **명시적이고 예측 가능하며 유지보수가 쉬운** 백엔드 시스템을 만든다.

---

## 1. Error as Value - 에러 처리 패턴

### 문제점: Exception의 남용

**안티패턴 ❌**:
```java
// 비즈니스 로직에서 Exception을 제어 흐름으로 사용
public User getUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

    if (!user.isActive()) {
        throw new UserInactiveException("User is inactive");
    }

    return user;
}

// 호출 측에서 여러 exception을 처리해야 함
try {
    User user = userService.getUserById(id);
    // ...
} catch (UserNotFoundException e) {
    // handle
} catch (UserInactiveException e) {
    // handle
}
```

**문제점**:
- 예외가 메서드 시그니처에 나타나지 않음 (RuntimeException의 경우)
- 제어 흐름이 명시적이지 않음
- 성능 오버헤드 (stack trace 생성)
- 예측 가능한 비즈니스 에러를 예외로 처리

### 해결책: Result 패턴 (Error as Value)

**베스트 프랙티스 ✅**:

```java
// Result.java - Generic Result Type
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    record Success<T, E>(T value) implements Result<T, E> {
        public boolean isSuccess() { return true; }
        public boolean isFailure() { return false; }
    }

    record Failure<T, E>(E error) implements Result<T, E> {
        public boolean isSuccess() { return false; }
        public boolean isFailure() { return true; }
    }

    // Factory methods
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    // Utility methods
    default T getOrThrow() {
        return switch (this) {
            case Success<T, E> s -> s.value();
            case Failure<T, E> f -> throw new IllegalStateException("Result is failure");
        };
    }

    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T, E> s -> s.value();
            case Failure<T, E> f -> defaultValue;
        };
    }

    default <U> Result<U, E> map(Function<T, U> mapper) {
        return switch (this) {
            case Success<T, E> s -> Result.success(mapper.apply(s.value()));
            case Failure<T, E> f -> Result.failure(f.error());
        };
    }

    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        return switch (this) {
            case Success<T, E> s -> mapper.apply(s.value());
            case Failure<T, E> f -> Result.failure(f.error());
        };
    }
}

// UserError.java - 명시적인 에러 타입
public sealed interface UserError permits
    UserError.NotFound,
    UserError.Inactive,
    UserError.Unauthorized {

    record NotFound(Long userId) implements UserError {
        public String message() {
            return "User not found: " + userId;
        }
    }

    record Inactive(Long userId) implements UserError {
        public String message() {
            return "User is inactive: " + userId;
        }
    }

    record Unauthorized(Long userId, String reason) implements UserError {
        public String message() {
            return "Unauthorized access to user " + userId + ": " + reason;
        }
    }
}

// UserService.java - Result를 사용한 서비스
@Service
public class UserService {

    private final UserRepository userRepository;

    public Result<User, UserError> getUserById(Long id) {
        return userRepository.findById(id)
            .map(user -> {
                if (!user.isActive()) {
                    return Result.<User, UserError>failure(new UserError.Inactive(id));
                }
                return Result.<User, UserError>success(user);
            })
            .orElse(Result.failure(new UserError.NotFound(id)));
    }

    public Result<User, UserError> updateUser(Long id, UpdateUserRequest request) {
        return getUserById(id)
            .flatMap(user -> {
                user.update(request);
                User saved = userRepository.save(user);
                return Result.success(saved);
            });
    }
}

// UserController.java - 명시적인 에러 처리
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        Result<User, UserError> result = userService.getUserById(id);

        return switch (result) {
            case Result.Success<User, UserError> s ->
                ResponseEntity.ok(UserResponse.from(s.value()));

            case Result.Failure<User, UserError> f -> switch (f.error()) {
                case UserError.NotFound notFound ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.of(notFound.message()));

                case UserError.Inactive inactive ->
                    ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.of(inactive.message()));

                case UserError.Unauthorized unauthorized ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.of(unauthorized.message()));
            };
        };
    }
}
```

**장점**:
- ✅ 에러가 타입 시스템에 명시적으로 표현됨
- ✅ 제어 흐름이 명확함
- ✅ 컴파일러가 모든 케이스 처리를 강제함 (sealed interface + switch expression)
- ✅ 성능 오버헤드 없음
- ✅ 함수형 프로그래밍 패턴 적용 가능 (map, flatMap)

### 언제 Exception을 사용할까?

**Exception은 다음 경우에만 사용**:
1. **시스템 레벨 에러**: `OutOfMemoryError`, `StackOverflowError` 등
2. **외부 시스템 장애**: DB 연결 실패, 네트워크 타임아웃 등
3. **프로그래밍 오류**: `NullPointerException`, `IllegalArgumentException` 등
4. **복구 불가능한 에러**: 애플리케이션이 계속 실행될 수 없는 상태

**예측 가능한 비즈니스 에러는 Result 패턴 사용**:
- 사용자를 찾을 수 없음
- 권한 없음
- 유효하지 않은 입력
- 비즈니스 규칙 위반

---

## 2. Composition over Inheritance - 컴포지션 우선

### 문제점: 상속의 남용

**안티패턴 ❌**:
```java
// 깊은 상속 계층
public abstract class BaseEntity {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public abstract class AuditableEntity extends BaseEntity {
    private String createdBy;
    private String updatedBy;
}

public abstract class SoftDeletableEntity extends AuditableEntity {
    private boolean deleted;
    private LocalDateTime deletedAt;
}

public class User extends SoftDeletableEntity {
    private String username;
    private String email;
    // User는 모든 부모 클래스의 메서드와 필드를 상속받음
}

// 서비스도 상속으로 구현
public abstract class BaseService<T extends BaseEntity> {
    protected abstract JpaRepository<T, Long> getRepository();

    public T save(T entity) {
        return getRepository().save(entity);
    }
}

public class UserService extends BaseService<User> {
    private final UserRepository userRepository;

    @Override
    protected JpaRepository<User, Long> getRepository() {
        return userRepository;
    }
    // 상속으로 인해 유연성 부족
}
```

**문제점**:
- 깊은 상속 계층으로 인한 복잡도 증가
- 변경 시 모든 자식 클래스 영향
- 다중 상속 불가능
- 런타임에 동작 변경 불가능
- 테스트 어려움

### 해결책: Composition + Interface

**베스트 프랙티스 ✅**:

```java
// 1. 작고 명확한 인터페이스들
public interface Identifiable {
    Long getId();
    void setId(Long id);
}

public interface Timestamped {
    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);
    LocalDateTime getUpdatedAt();
    void setUpdatedAt(LocalDateTime updatedAt);
}

public interface Auditable {
    String getCreatedBy();
    void setCreatedBy(String createdBy);
    String getUpdatedBy();
    void setUpdatedBy(String updatedBy);
}

public interface SoftDeletable {
    boolean isDeleted();
    void markAsDeleted();
    void restore();
    LocalDateTime getDeletedAt();
}

// 2. 컴포지션을 사용한 구현
@Entity
@Table(name = "users")
public class User implements Identifiable, Timestamped, Auditable, SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 컴포지션: 공통 기능을 별도 객체로 분리
    @Embedded
    private final TimestampInfo timestampInfo = new TimestampInfo();

    @Embedded
    private final AuditInfo auditInfo = new AuditInfo();

    @Embedded
    private final SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();

    // User 고유 필드
    private String username;
    private String email;

    // Identifiable 구현
    @Override
    public Long getId() { return id; }

    @Override
    public void setId(Long id) { this.id = id; }

    // Timestamped 구현 - 내부 객체에 위임
    @Override
    public LocalDateTime getCreatedAt() { return timestampInfo.getCreatedAt(); }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        timestampInfo.setCreatedAt(createdAt);
    }

    @Override
    public LocalDateTime getUpdatedAt() { return timestampInfo.getUpdatedAt(); }

    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) {
        timestampInfo.setUpdatedAt(updatedAt);
    }

    // Auditable 구현 - 내부 객체에 위임
    @Override
    public String getCreatedBy() { return auditInfo.getCreatedBy(); }

    @Override
    public void setCreatedBy(String createdBy) { auditInfo.setCreatedBy(createdBy); }

    @Override
    public String getUpdatedBy() { return auditInfo.getUpdatedBy(); }

    @Override
    public void setUpdatedBy(String updatedBy) { auditInfo.setUpdatedBy(updatedBy); }

    // SoftDeletable 구현 - 내부 객체에 위임
    @Override
    public boolean isDeleted() { return softDeleteInfo.isDeleted(); }

    @Override
    public void markAsDeleted() { softDeleteInfo.markAsDeleted(); }

    @Override
    public void restore() { softDeleteInfo.restore(); }

    @Override
    public LocalDateTime getDeletedAt() { return softDeleteInfo.getDeletedAt(); }
}

// 3. Embeddable 클래스들 (재사용 가능한 컴포넌트)
@Embeddable
public class TimestampInfo {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters and setters
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

@Embeddable
public class AuditInfo {
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // getters and setters
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}

@Embeddable
public class SoftDeleteInfo {
    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() { return deleted; }

    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }
}

// 4. 서비스도 컴포지션으로 설계
@Service
public class UserService {

    // 상속 대신 컴포지션으로 공통 기능 재사용
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    public UserService(
        UserRepository userRepository,
        ValidationService validationService,
        AuditService auditService,
        EventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.validationService = validationService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    public Result<User, UserError> createUser(CreateUserRequest request) {
        // 각 서비스의 기능을 조합
        return validationService.validate(request)
            .flatMap(validRequest -> {
                User user = User.from(validRequest);
                auditService.setCreatedBy(user);
                User saved = userRepository.save(user);
                eventPublisher.publish(new UserCreatedEvent(saved));
                return Result.success(saved);
            });
    }
}
```

**장점**:
- ✅ 유연한 설계: 런타임에 동작 변경 가능
- ✅ 단일 책임 원칙: 각 컴포넌트가 하나의 역할만 수행
- ✅ 테스트 용이: 각 컴포넌트를 독립적으로 테스트 가능
- ✅ 재사용성: 컴포넌트를 다양한 곳에서 재사용 가능
- ✅ 낮은 결합도: 변경 시 영향 범위 최소화

### Golang의 Struct Embedding과 비교

```go
// Golang의 컴포지션 (struct embedding)
type TimestampInfo struct {
    CreatedAt time.Time
    UpdatedAt time.Time
}

type AuditInfo struct {
    CreatedBy string
    UpdatedBy string
}

type User struct {
    ID            int64
    TimestampInfo          // embedding
    AuditInfo             // embedding
    Username      string
    Email         string
}

// 자동으로 필드에 접근 가능
user.CreatedAt // TimestampInfo.CreatedAt
user.CreatedBy // AuditInfo.CreatedBy
```

Java에서는 embedding이 없지만, `@Embedded`와 delegation을 통해 유사한 효과를 얻을 수 있습니다.

---

## 3. Static Factory Methods - 정적 팩토리 메서드

### 문제점: 생성자의 한계

**안티패턴 ❌**:
```java
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;

    // 여러 생성자 - 의미가 불명확
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // 같은 타입의 파라미터를 갖는 생성자는 만들 수 없음
    // public User(String email, String username) { } // 컴파일 에러

    // 생성 실패 시 null 반환 또는 exception 발생
    public User(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        this.username = username;
    }
}

// 사용 시 의미가 불명확
User user1 = new User("john", "john@example.com");
User user2 = new User("john@example.com", "john"); // 어떤 순서인지 헷갈림
```

**문제점**:
- 생성자 이름이 클래스 이름으로 고정
- 같은 시그니처의 생성자를 여러 개 만들 수 없음
- 생성 실패 시 null 또는 exception만 가능
- 매번 새 객체를 생성해야 함 (캐싱 불가)
- 의미 전달이 어려움

### 해결책: Static Factory Methods

**베스트 프랙티스 ✅**:

```java
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private UserStatus status;

    // Private 생성자 - 직접 생성 방지
    private User() {}

    // 명확한 이름을 가진 팩토리 메서드들
    public static Result<User, UserError> create(
        String username,
        String email,
        String password
    ) {
        // 유효성 검증
        if (username == null || username.isBlank()) {
            return Result.failure(new UserError.InvalidInput("Username is required"));
        }
        if (email == null || !email.contains("@")) {
            return Result.failure(new UserError.InvalidInput("Invalid email"));
        }
        if (password == null || password.length() < 8) {
            return Result.failure(new UserError.InvalidInput("Password must be at least 8 characters"));
        }

        User user = new User();
        user.username = username;
        user.email = email;
        user.password = hashPassword(password);
        user.status = UserStatus.ACTIVE;

        return Result.success(user);
    }

    // 다른 생성 시나리오를 위한 팩토리 메서드
    public static User createInactive(String username, String email) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.status = UserStatus.INACTIVE;
        return user;
    }

    // DTO로부터 생성
    public static Result<User, UserError> from(CreateUserRequest request) {
        return create(request.username(), request.email(), request.password());
    }

    // 복원 (DB로부터 로드)
    public static User restore(
        Long id,
        String username,
        String email,
        String hashedPassword,
        UserStatus status
    ) {
        User user = new User();
        user.id = id;
        user.username = username;
        user.email = email;
        user.password = hashedPassword;
        user.status = status;
        return user;
    }

    // 캐싱이 필요한 경우
    private static final Map<String, User> GUEST_CACHE = new ConcurrentHashMap<>();

    public static User guest(String sessionId) {
        return GUEST_CACHE.computeIfAbsent(sessionId, id -> {
            User user = new User();
            user.username = "guest_" + id;
            user.status = UserStatus.GUEST;
            return user;
        });
    }

    private static String hashPassword(String password) {
        // BCrypt 등을 사용한 해싱
        return password; // simplified
    }
}

// 사용 예제
class UserService {
    public Result<User, UserError> registerUser(CreateUserRequest request) {
        // 명확한 의도 전달
        Result<User, UserError> userResult = User.create(
            request.username(),
            request.email(),
            request.password()
        );

        return userResult.flatMap(user -> {
            User saved = userRepository.save(user);
            return Result.success(saved);
        });
    }
}
```

### Golang의 Constructor Function 패턴과 비교

```go
// Golang의 constructor function pattern
package user

type User struct {
    id       int64
    username string
    email    string
    password string
}

// Package-level constructor function
func NewUser(username, email, password string) (*User, error) {
    if username == "" {
        return nil, errors.New("username is required")
    }
    if email == "" {
        return nil, errors.New("email is required")
    }

    return &User{
        username: username,
        email:    email,
        password: hashPassword(password),
    }, nil
}

// 다른 생성 시나리오
func NewInactiveUser(username, email string) *User {
    return &User{
        username: username,
        email:    email,
    }
}

func RestoreUser(id int64, username, email, hashedPassword string) *User {
    return &User{
        id:       id,
        username: username,
        email:    email,
        password: hashedPassword,
    }
}
```

Java의 static factory method는 Golang의 package-level constructor function과 같은 역할을 합니다.

### 팩토리 메서드 네이밍 컨벤션

| 이름 | 용도 | 예제 |
|------|------|------|
| `create()` | 새 인스턴스 생성 (검증 포함) | `User.create(username, email, password)` |
| `of()` | 파라미터를 기반으로 인스턴스 생성 | `Money.of(100, Currency.USD)` |
| `from()` | 다른 타입으로부터 변환 | `User.from(userDto)` |
| `valueOf()` | 값 기반 변환 | `Integer.valueOf("123")` |
| `instance()` / `getInstance()` | 싱글톤 또는 캐시된 인스턴스 | `User.guest(sessionId)` |
| `restore()` | 저장된 데이터로부터 복원 | `User.restore(id, username, ...)` |
| `builder()` | 빌더 패턴 시작 | `User.builder().username("john")...` |

---

## 4. Dependency Injection & IoC - 의존성 관리

### 핵심 원칙

1. **의존성 주입 (DI)**: 객체가 자신의 의존성을 직접 생성하지 않고 외부로부터 주입받음
2. **제어 역전 (IoC)**: 프레임워크가 객체의 생명주기와 의존성을 관리
3. **의존성 역전 원칙 (DIP)**: 구체적인 구현이 아닌 추상화에 의존

### 안티패턴: 직접 의존성 생성

**안티패턴 ❌**:
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    // 의존성을 직접 생성 - 강한 결합
    private final UserService userService = new UserService();

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}

public class UserService {
    // 구체 클래스에 직접 의존
    private final UserRepository userRepository = new UserRepositoryImpl();
    private final EmailService emailService = new EmailServiceImpl();

    public User getUserById(Long id) {
        return userRepository.findById(id);
    }
}
```

**문제점**:
- 테스트 어려움 (Mock 사용 불가)
- 구현 변경 시 모든 코드 수정 필요
- 순환 의존성 발생 가능
- 객체 생명주기 관리 어려움

### 베스트 프랙티스: Constructor Injection

**베스트 프랙티스 ✅**:

```java
// 1. 인터페이스 정의 (추상화)
public interface UserRepository {
    Optional<User> findById(Long id);
    User save(User user);
    List<User> findByStatus(UserStatus status);
}

public interface EmailService {
    Result<Void, EmailError> sendWelcomeEmail(User user);
}

public interface EventPublisher {
    void publish(DomainEvent event);
}

// 2. Service는 인터페이스에 의존
@Service
public class UserService {

    // Final 필드 + 생성자 주입 (불변성 보장)
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EventPublisher eventPublisher;

    // 생성자 주입 - Spring이 자동으로 의존성 주입
    // @Autowired는 생성자가 하나일 경우 생략 가능
    public UserService(
        UserRepository userRepository,
        EmailService emailService,
        EventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
    }

    public Result<User, UserError> getUserById(Long id) {
        return userRepository.findById(id)
            .map(user -> Result.<User, UserError>success(user))
            .orElse(Result.failure(new UserError.NotFound(id)));
    }

    public Result<User, UserError> createUser(CreateUserRequest request) {
        return User.create(request.username(), request.email(), request.password())
            .flatMap(user -> {
                User saved = userRepository.save(user);
                emailService.sendWelcomeEmail(saved);
                eventPublisher.publish(new UserCreatedEvent(saved));
                return Result.success(saved);
            });
    }
}

// 3. Controller도 생성자 주입
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
            .map(user -> ResponseEntity.ok(UserResponse.from(user)))
            .getOrElse(ResponseEntity.notFound().build());
    }
}

// 4. 구현체들
@Repository
public class JpaUserRepository implements UserRepository {

    private final JpaUserEntityRepository jpaRepository;

    public JpaUserRepository(JpaUserEntityRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
            .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return jpaRepository.findByStatus(status.name())
            .stream()
            .map(UserEntity::toDomain)
            .toList();
    }
}

@Service
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateEngine templateEngine;

    public SmtpEmailService(
        JavaMailSender mailSender,
        EmailTemplateEngine templateEngine
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public Result<Void, EmailError> sendWelcomeEmail(User user) {
        try {
            String content = templateEngine.render("welcome", Map.of("user", user));
            MimeMessage message = mailSender.createMimeMessage();
            // ... 이메일 설정
            mailSender.send(message);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(new EmailError.SendFailed(e.getMessage()));
        }
    }
}
```

### Golang의 의존성 주입 패턴

```go
// Golang에서는 명시적으로 의존성을 주입
package service

type UserRepository interface {
    FindByID(id int64) (*User, error)
    Save(user *User) error
}

type EmailService interface {
    SendWelcomeEmail(user *User) error
}

type UserService struct {
    userRepo     UserRepository
    emailService EmailService
}

// Constructor function으로 의존성 주입
func NewUserService(
    userRepo UserRepository,
    emailService EmailService,
) *UserService {
    return &UserService{
        userRepo:     userRepo,
        emailService: emailService,
    }
}

func (s *UserService) CreateUser(req CreateUserRequest) (*User, error) {
    user, err := NewUser(req.Username, req.Email, req.Password)
    if err != nil {
        return nil, err
    }

    if err := s.userRepo.Save(user); err != nil {
        return nil, err
    }

    // Best effort - 이메일 실패해도 사용자 생성은 성공
    _ = s.emailService.SendWelcomeEmail(user)

    return user, nil
}

// main.go에서 의존성 조립
func main() {
    db := setupDatabase()

    // 구현체 생성
    userRepo := repository.NewUserRepository(db)
    emailService := email.NewSmtpEmailService(smtpConfig)

    // 의존성 주입
    userService := service.NewUserService(userRepo, emailService)
    userController := controller.NewUserController(userService)

    // ...
}
```

Java Spring은 DI 컨테이너가 자동으로 의존성을 주입하지만, Golang은 명시적으로 의존성을 조립합니다. 둘 다 장단점이 있습니다:

- **Java Spring**: 자동 주입으로 편리하지만, 런타임에 문제 발견
- **Golang**: 명시적이고 컴파일 타임에 안전하지만, 보일러플레이트 코드 증가

### DI 사용 시 주의사항

**✅ DO**:
- Constructor injection 사용 (불변성 보장)
- 인터페이스에 의존 (구현체가 아닌)
- 필드를 final로 선언
- 단일 생성자 사용 (@Autowired 생략 가능)

**❌ DON'T**:
- Field injection 사용 (`@Autowired private UserService userService`)
- Setter injection 사용 (불변성 깨짐)
- 구체 클래스에 직접 의존
- 순환 의존성 생성

```java
// Field Injection - 피할 것 ❌
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository; // 테스트 어려움, 불변성 보장 안됨
}

// Constructor Injection - 권장 ✅
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

---

## 5. Separation of Concerns - 책임 분리

### 레이어드 아키텍처

**프로덕션 레벨 패키지 구조**:

```
com.example.backend/
├── domain/                    # 도메인 계층 (비즈니스 로직)
│   ├── user/
│   │   ├── User.java         # 도메인 모델
│   │   ├── UserRepository.java  # 저장소 인터페이스
│   │   ├── UserService.java     # 비즈니스 로직
│   │   ├── UserError.java       # 도메인 에러
│   │   └── event/
│   │       └── UserCreatedEvent.java
│   ├── order/
│   └── payment/
│
├── application/               # 애플리케이션 계층 (유스케이스)
│   ├── user/
│   │   ├── CreateUserUseCase.java
│   │   ├── UpdateUserUseCase.java
│   │   └── dto/
│   │       ├── CreateUserRequest.java
│   │       └── UserResponse.java
│   └── order/
│
├── infrastructure/            # 인프라 계층 (기술적 구현)
│   ├── persistence/
│   │   ├── user/
│   │   │   ├── JpaUserRepository.java  # Repository 구현
│   │   │   ├── UserEntity.java         # JPA Entity
│   │   │   └── UserJpaRepository.java  # Spring Data JPA
│   │   └── order/
│   ├── messaging/
│   │   ├── KafkaEventPublisher.java
│   │   └── KafkaEventListener.java
│   ├── email/
│   │   └── SmtpEmailService.java
│   └── cache/
│       └── RedisCacheService.java
│
└── presentation/              # 표현 계층 (API)
    ├── api/
    │   ├── user/
    │   │   ├── UserController.java
    │   │   └── dto/
    │   │       ├── UserApiRequest.java
    │   │       └── UserApiResponse.java
    │   └── order/
    └── exception/
        └── GlobalExceptionHandler.java
```

### 계층별 책임

**1. Domain Layer (도메인 계층)**

```java
// domain/user/User.java
public class User {
    private Long id;
    private String username;
    private String email;
    private UserStatus status;

    // 비즈니스 로직은 도메인 모델에 위치
    public Result<Void, UserError> activate() {
        if (this.status == UserStatus.BANNED) {
            return Result.failure(new UserError.BannedUser(this.id));
        }
        this.status = UserStatus.ACTIVE;
        return Result.success(null);
    }

    public Result<Void, UserError> changeEmail(String newEmail) {
        if (newEmail == null || !newEmail.contains("@")) {
            return Result.failure(new UserError.InvalidEmail(newEmail));
        }
        this.email = newEmail;
        return Result.success(null);
    }

    public boolean canPlaceOrder() {
        return this.status == UserStatus.ACTIVE;
    }
}

// domain/user/UserRepository.java - 인터페이스만 정의
public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user);
    List<User> findActiveUsers();
}

// domain/user/UserService.java - 도메인 서비스 (복잡한 비즈니스 로직)
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 비즈니스 규칙: 이메일 중복 검증
    public Result<User, UserError> registerUser(
        String username,
        String email,
        String rawPassword
    ) {
        // 중복 검증
        if (userRepository.findByEmail(email).isPresent()) {
            return Result.failure(new UserError.EmailAlreadyExists(email));
        }

        // 도메인 객체 생성
        return User.create(username, email, rawPassword)
            .map(user -> {
                String encoded = passwordEncoder.encode(rawPassword);
                user.setPassword(encoded);
                return userRepository.save(user);
            });
    }
}
```

**2. Application Layer (애플리케이션 계층)**

```java
// application/user/CreateUserUseCase.java
@Component
public class CreateUserUseCase {

    private final UserService userService;
    private final EmailService emailService;
    private final EventPublisher eventPublisher;

    public CreateUserUseCase(
        UserService userService,
        EmailService emailService,
        EventPublisher eventPublisher
    ) {
        this.userService = userService;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Result<UserResponse, UserError> execute(CreateUserRequest request) {
        // 1. 비즈니스 로직 실행
        Result<User, UserError> userResult = userService.registerUser(
            request.username(),
            request.email(),
            request.password()
        );

        // 2. 부가 작업 (이메일, 이벤트)
        return userResult.map(user -> {
            // Best effort - 실패해도 사용자 생성은 성공
            emailService.sendWelcomeEmail(user);

            // 도메인 이벤트 발행
            eventPublisher.publish(new UserCreatedEvent(user));

            return UserResponse.from(user);
        });
    }
}

// application/user/dto/CreateUserRequest.java
public record CreateUserRequest(
    @NotBlank String username,
    @Email String email,
    @Size(min = 8) String password
) {}

// application/user/dto/UserResponse.java
public record UserResponse(
    Long id,
    String username,
    String email,
    String status
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getStatus().name()
        );
    }
}
```

**3. Infrastructure Layer (인프라 계층)**

```java
// infrastructure/persistence/user/JpaUserRepository.java
@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataRepo;

    public JpaUserRepository(SpringDataUserRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataRepo.findById(id)
            .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepo.findByEmail(email)
            .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity saved = springDataRepo.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<User> findActiveUsers() {
        return springDataRepo.findByStatus(UserStatus.ACTIVE.name())
            .stream()
            .map(UserEntity::toDomain)
            .toList();
    }
}

// infrastructure/persistence/user/UserEntity.java
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // Domain ↔ Entity 변환
    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.id = user.getId();
        entity.username = user.getUsername();
        entity.email = user.getEmail();
        entity.password = user.getPassword();
        entity.status = user.getStatus();
        return entity;
    }

    public User toDomain() {
        return User.restore(id, username, email, password, status);
    }
}

// infrastructure/messaging/KafkaEventPublisher.java
@Component
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(event.getTopicName(), json);
        } catch (JsonProcessingException e) {
            // 로깅 후 무시 (이벤트 발행 실패가 주 로직을 방해하면 안됨)
            log.error("Failed to publish event: {}", event, e);
        }
    }
}
```

**4. Presentation Layer (표현 계층)**

```java
// presentation/api/user/UserController.java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;

    public UserController(
        CreateUserUseCase createUserUseCase,
        GetUserUseCase getUserUseCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        Result<UserResponse, UserError> result = createUserUseCase.execute(request);

        return switch (result) {
            case Result.Success<UserResponse, UserError> s ->
                ResponseEntity.status(HttpStatus.CREATED).body(s.value());

            case Result.Failure<UserResponse, UserError> f -> switch (f.error()) {
                case UserError.EmailAlreadyExists e ->
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ErrorResponse.of("EMAIL_EXISTS", e.message()));

                case UserError.InvalidInput e ->
                    ResponseEntity.badRequest()
                        .body(ErrorResponse.of("INVALID_INPUT", e.message()));

                default ->
                    ResponseEntity.internalServerError()
                        .body(ErrorResponse.of("INTERNAL_ERROR", "An error occurred"));
            };
        };
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Result<UserResponse, UserError> result = getUserUseCase.execute(id);

        return result
            .map(user -> ResponseEntity.ok(user))
            .getOrElse(ResponseEntity.notFound().build());
    }
}

// presentation/exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 시스템 예외만 여기서 처리 (비즈니스 에러는 Result로 처리)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.internalServerError()
            .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("VALIDATION_ERROR", message));
    }
}
```

### 계층 간 의존성 규칙

```
Presentation Layer (Controller)
    ↓ (의존)
Application Layer (UseCase)
    ↓ (의존)
Domain Layer (Service, Model)
    ↑ (구현)
Infrastructure Layer (Repository Impl, External Services)
```

**핵심 규칙**:
1. **상위 레이어는 하위 레이어에 의존**할 수 있음
2. **하위 레이어는 상위 레이어에 의존하면 안 됨**
3. **Domain Layer는 다른 레이어에 의존하지 않음** (순수 비즈니스 로직)
4. **Infrastructure Layer는 Domain의 인터페이스를 구현**

---

## 6. Anti-Patterns - 피해야 할 패턴

### 1. God Object (신 객체)

**안티패턴 ❌**:
```java
// 모든 것을 다 하는 거대한 클래스
@Service
public class UserManager {
    // 너무 많은 의존성
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    // 사용자 관련
    public User createUser(...) { }
    public User updateUser(...) { }
    public void deleteUser(...) { }

    // 주문 관련 (왜 UserManager에?)
    public Order createOrder(...) { }
    public void cancelOrder(...) { }

    // 결제 관련 (왜 UserManager에?)
    public Payment processPayment(...) { }

    // 알림 관련 (왜 UserManager에?)
    public void sendNotification(...) { }

    // 1000+ 줄의 코드...
}
```

**해결책 ✅**:
```java
// 각 책임별로 분리
@Service
public class UserService {
    // 사용자 관련 로직만
}

@Service
public class OrderService {
    // 주문 관련 로직만
}

@Service
public class PaymentService {
    // 결제 관련 로직만
}

@Service
public class NotificationService {
    // 알림 관련 로직만
}
```

### 2. Anemic Domain Model (빈약한 도메인 모델)

**안티패턴 ❌**:
```java
// 데이터만 가진 도메인 모델
public class Order {
    private Long id;
    private List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;

    // getter/setter만 있음
}

// 모든 로직이 서비스에 존재
@Service
public class OrderService {
    public void cancelOrder(Order order) {
        // 비즈니스 로직이 서비스에 위치
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }
        order.setStatus(OrderStatus.CANCELLED);

        // 환불 처리
        for (OrderItem item : order.getItems()) {
            refundItem(item);
        }

        // 총액 재계산
        BigDecimal refundAmount = order.getItems().stream()
            .map(OrderItem::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(order.getTotalAmount().subtract(refundAmount));
    }
}
```

**해결책 ✅**:
```java
// Rich Domain Model - 비즈니스 로직을 도메인 모델에 위치
public class Order {
    private Long id;
    private List<OrderItem> items;
    private OrderStatus status;
    private Money totalAmount;

    // 비즈니스 로직을 도메인 모델에
    public Result<Void, OrderError> cancel() {
        if (status == OrderStatus.COMPLETED) {
            return Result.failure(new OrderError.CannotCancelCompletedOrder(id));
        }

        if (status == OrderStatus.CANCELLED) {
            return Result.failure(new OrderError.AlreadyCancelled(id));
        }

        this.status = OrderStatus.CANCELLED;
        return Result.success(null);
    }

    public Result<Void, OrderError> addItem(OrderItem item) {
        if (status != OrderStatus.PENDING) {
            return Result.failure(new OrderError.CannotModifyOrder(id));
        }

        items.add(item);
        recalculateTotal();
        return Result.success(null);
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getPrice)
            .reduce(Money.ZERO, Money::add);
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
}

// 서비스는 조율만 담당
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RefundService refundService;

    @Transactional
    public Result<Void, OrderError> cancelOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .map(order -> order.cancel()
                .flatMap(v -> {
                    orderRepository.save(order);
                    refundService.processRefund(order);
                    return Result.success(null);
                }))
            .orElse(Result.failure(new OrderError.NotFound(orderId)));
    }
}
```

### 3. Primitive Obsession (원시 타입 집착)

**안티패턴 ❌**:
```java
public class Product {
    private String id;          // UUID인지 숫자인지 불명확
    private String name;
    private double price;       // 어떤 통화? 소수점 처리는?
    private String currency;    // 별도 필드로 분리
    private String email;       // 유효성 검증은?
    private String phoneNumber; // 형식은?
}

// 파라미터도 원시 타입
public void updatePrice(String productId, double newPrice, String currency) {
    // newPrice가 음수면? currency가 유효하지 않으면?
}
```

**해결책 ✅**:
```java
// Value Object 사용
public record ProductId(String value) {
    public ProductId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be blank");
        }
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }
}

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Email {
        if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email: " + value);
        }
    }
}

public record PhoneNumber(String value) {
    public PhoneNumber {
        if (value == null || !value.matches("\\d{10,11}")) {
            throw new IllegalArgumentException("Invalid phone number: " + value);
        }
    }

    public String getFormatted() {
        // 010-1234-5678 형식으로 반환
        return value.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }
}

// Value Object를 사용한 도메인 모델
public class Product {
    private ProductId id;
    private String name;
    private Money price;
    private Email contactEmail;
    private PhoneNumber contactPhone;

    // 타입 안전성 확보
    public Result<Void, ProductError> updatePrice(Money newPrice) {
        if (newPrice.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(new ProductError.InvalidPrice());
        }
        this.price = newPrice;
        return Result.success(null);
    }
}
```

### 4. Magic Numbers and Strings

**안티패턴 ❌**:
```java
public class OrderService {
    public void processOrder(Order order) {
        if (order.getStatus() == 1) {  // 1이 무엇을 의미?
            // ...
        }

        if (order.getTotalAmount() > 100000) {  // 100000이 무엇을 의미?
            applyDiscount(order, 0.1);  // 0.1은?
        }

        sendEmail(order, "ORDER_CONFIRMED");  // 문자열 하드코딩
    }
}
```

**해결책 ✅**:
```java
// Enum 사용
public enum OrderStatus {
    PENDING(1),
    CONFIRMED(2),
    SHIPPED(3),
    DELIVERED(4),
    CANCELLED(5);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }
}

// 상수 사용
public class OrderPolicy {
    public static final Money VIP_THRESHOLD = Money.of(100_000, Currency.KRW);
    public static final BigDecimal VIP_DISCOUNT_RATE = new BigDecimal("0.10");
}

public enum EmailTemplate {
    ORDER_CONFIRMED("order_confirmed"),
    ORDER_SHIPPED("order_shipped"),
    ORDER_DELIVERED("order_delivered");

    private final String templateName;

    EmailTemplate(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}

// 명확한 코드
public class OrderService {
    public void processOrder(Order order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            // ...
        }

        if (order.getTotalAmount().isGreaterThan(OrderPolicy.VIP_THRESHOLD)) {
            applyDiscount(order, OrderPolicy.VIP_DISCOUNT_RATE);
        }

        emailService.send(order, EmailTemplate.ORDER_CONFIRMED);
    }
}
```

### 5. Repository에서 비즈니스 로직 수행

**안티패턴 ❌**:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Repository가 비즈니스 로직을 수행 (잘못된 책임)
    @Query("UPDATE User u SET u.status = 'ACTIVE', u.lastLoginAt = :now WHERE u.id = :id")
    @Modifying
    void activateUser(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :id")
    @Modifying
    void incrementFailedLoginAttempts(@Param("id") Long id);
}
```

**해결책 ✅**:
```java
// Repository는 단순 데이터 접근만
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user);
}

// 비즈니스 로직은 도메인 모델에
public class User {
    public Result<Void, UserError> activate() {
        if (this.status == UserStatus.BANNED) {
            return Result.failure(new UserError.CannotActivateBannedUser(this.id));
        }
        this.status = UserStatus.ACTIVE;
        this.lastLoginAt = LocalDateTime.now();
        return Result.success(null);
    }

    public Result<Void, UserError> recordFailedLogin() {
        this.failedLoginAttempts++;

        if (this.failedLoginAttempts >= 5) {
            this.status = UserStatus.LOCKED;
            return Result.failure(new UserError.AccountLocked(this.id));
        }

        return Result.success(null);
    }
}

// 서비스에서 조율
@Service
public class UserService {
    public Result<Void, UserError> activateUser(Long userId) {
        return userRepository.findById(userId)
            .map(user -> user.activate()
                .map(v -> {
                    userRepository.save(user);
                    return v;
                }))
            .orElse(Result.failure(new UserError.NotFound(userId)));
    }
}
```

---

## 7. 실전 적용 가이드

### 프로젝트 시작 시 체크리스트

**✅ 아키텍처 설정**:
- [ ] 레이어드 아키텍처로 패키지 구조 설계
- [ ] Result 타입 구현 (또는 Vavr, Arrow 등 라이브러리 사용)
- [ ] 공통 에러 타입 정의
- [ ] Value Object 패턴 적용 (Money, Email 등)

**✅ 의존성 관리**:
- [ ] Constructor injection 사용
- [ ] 인터페이스 우선 설계
- [ ] 순환 의존성 제거

**✅ 에러 처리 전략**:
- [ ] 비즈니스 에러는 Result 패턴 사용
- [ ] 시스템 에러는 Exception 사용
- [ ] GlobalExceptionHandler 설정

**✅ 도메인 모델**:
- [ ] Rich Domain Model 적용 (비즈니스 로직을 도메인에)
- [ ] Static Factory Method 사용
- [ ] Composition over Inheritance

### 팀 코딩 컨벤션 예시

```java
/**
 * 팀 코딩 컨벤션
 *
 * 1. Error Handling
 *    - 비즈니스 에러: Result<T, E> 사용
 *    - 시스템 에러: Exception 사용
 *
 * 2. Object Creation
 *    - Static factory method 사용 (new 키워드 최소화)
 *    - Private constructor로 직접 생성 방지
 *
 * 3. Dependency Injection
 *    - Constructor injection 사용
 *    - Final 필드로 불변성 보장
 *
 * 4. Domain Model
 *    - 비즈니스 로직은 도메인 모델에 위치
 *    - Value Object 적극 사용
 *
 * 5. Service Layer
 *    - UseCase별로 분리 (CreateUserUseCase, UpdateUserUseCase)
 *    - @Transactional은 Application Layer에서만
 *
 * 6. Naming Convention
 *    - Factory method: create, from, of, valueOf, restore
 *    - Error types: sealed interface로 정의
 *    - Repository: 인터페이스는 domain, 구현체는 infrastructure
 */
```

### 점진적 도입 전략

기존 프로젝트에 이러한 패턴을 도입할 때는 점진적으로 적용하세요:

**1단계: Result 패턴 도입**
```java
// 기존: Exception 사용
public User getUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}

// 개선: Result 사용
public Result<User, UserError> getUser(Long id) {
    return userRepository.findById(id)
        .map(user -> Result.<User, UserError>success(user))
        .orElse(Result.failure(new UserError.NotFound(id)));
}
```

**2단계: Static Factory Method 도입**
```java
// 기존: Public constructor
public User(String username, String email) {
    this.username = username;
    this.email = email;
}

// 개선: Static factory method
private User() {}

public static Result<User, UserError> create(String username, String email) {
    // 유효성 검증 + 생성
}
```

**3단계: Value Object 도입**
```java
// 기존: Primitive type
private String email;
private double price;

// 개선: Value Object
private Email email;
private Money price;
```

**4단계: Rich Domain Model**
```java
// 기존: Anemic model
public class User {
    private String status;
    // getters/setters only
}

@Service
public class UserService {
    public void activateUser(User user) {
        user.setStatus("ACTIVE");
    }
}

// 개선: Rich domain model
public class User {
    private UserStatus status;

    public Result<Void, UserError> activate() {
        // 비즈니스 규칙 검증
        this.status = UserStatus.ACTIVE;
        return Result.success(null);
    }
}
```

### 성능 고려사항

**Result 패턴이 성능에 미치는 영향**:
- ✅ Exception보다 훨씬 빠름 (stack trace 생성 없음)
- ✅ 메모리 효율적 (작은 객체)
- ⚠️ GC 압력 증가 (객체 생성량 증가)

**최적화 팁**:
```java
// 자주 사용되는 실패 케이스는 캐싱
public sealed interface UserError {
    record NotFound(Long userId) implements UserError {
        private static final Map<Long, NotFound> CACHE = new ConcurrentHashMap<>();

        public static NotFound of(Long userId) {
            return CACHE.computeIfAbsent(userId, NotFound::new);
        }
    }
}

// 성공 케이스도 재사용 가능
public class Result {
    private static final Result<Void, ?> SUCCESS_VOID = new Success<>(null);

    @SuppressWarnings("unchecked")
    public static <E> Result<Void, E> successVoid() {
        return (Result<Void, E>) SUCCESS_VOID;
    }
}
```

---

## 📚 참고 자료

### 서적
- **Effective Java** (Joshua Bloch) - Java best practices
- **Domain-Driven Design** (Eric Evans) - DDD 기초
- **Clean Architecture** (Robert C. Martin) - 아키텍처 설계
- **Functional Programming in Java** (Venkat Subramaniam)

### 오픈소스 프로젝트
- **Vavr**: Functional programming library for Java (Result, Either 등 제공)
- **Arrow**: Kotlin용이지만 Java에서도 참고 가능
- **Spring Modulith**: 모듈형 모놀리스 구조

### 온라인 리소스
- [Java Language Specification](https://docs.oracle.com/javase/specs/)
- [Spring Framework Documentation](https://spring.io/projects/spring-framework)
- [Golang Design Patterns](https://github.com/tmrts/go-patterns)

---

## 💡 핵심 요약

### Golang에서 가져올 것
1. **Error as Value**: 명시적이고 예측 가능한 에러 처리
2. **Composition**: struct embedding처럼 컴포지션 우선
3. **Explicit**: 암시적이지 않고 명시적인 코드
4. **Simple**: 복잡한 상속보다 간단한 조합

### Java에서 유지할 것
1. **Strong Typing**: 강력한 타입 시스템 활용
2. **DI Container**: Spring의 의존성 주입
3. **Rich Ecosystem**: JPA, Spring Data 등 검증된 도구
4. **OOP**: 객체지향 설계 원칙 (SOLID)

### 융합의 핵심
- **명시적 에러 처리**: Result<T, E> 패턴
- **컴포지션 우선**: @Embedded + Delegation
- **정적 팩토리**: 의미 있는 생성 메서드
- **의존성 주입**: 테스트 가능하고 유연한 설계
- **책임 분리**: 계층화된 아키텍처

---

**프로덕션 레벨에서 가장 중요한 것은 "일관성"입니다.**

팀 전체가 동일한 패턴을 따를 때 유지보수가 쉬워지고, 버그가 줄어들며, 새로운 팀원의 온보딩도 빨라집니다. 이 문서의 모든 패턴을 한번에 적용하려 하지 말고, 팀과 함께 하나씩 도입해보세요.

Happy coding! 🚀
