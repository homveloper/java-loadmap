# 현대적 Java 기능 활용

> Java 8-21의 실용적 기능들로 간결하고 안전한 코드 작성하기

## 목차
- [Record - 보일러플레이트 제거](#record---보일러플레이트-제거)
- [Optional - 올바른 null 처리](#optional---올바른-null-처리)
- [Stream API - 함수형 데이터 처리](#stream-api---함수형-데이터-처리)
- [정적 팩토리 메서드](#정적-팩토리-메서드)
- [Result 패턴 - Error as Value](#result-패턴---error-as-value)
- [Pattern Matching과 Sealed Classes](#pattern-matching과-sealed-classes)

---

## Record - 보일러플레이트 제거

### Before: 전통적인 DTO

```java
// ❌ Old Way: 50줄의 보일러플레이트
public class UserDto {
    private final String name;
    private final int age;
    private final String email;

    public UserDto(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return age == userDto.age &&
               Objects.equals(name, userDto.name) &&
               Objects.equals(email, userDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, email);
    }

    @Override
    public String toString() {
        return "UserDto{" +
               "name='" + name + '\'' +
               ", age=" + age +
               ", email='" + email + '\'' +
               '}';
    }
}
```

### After: Record (Java 14+)

```java
// ✅ Modern Way: 1줄!
public record UserDto(String name, int age, String email) {}

// 자동으로 제공:
// - 생성자
// - getter (getName() 아니라 name())
// - equals/hashCode
// - toString
// - 불변성 (final)
```

**사용 예시:**

```java
// 생성
UserDto user = new UserDto("John", 30, "john@example.com");

// getter (메서드명이 필드명과 동일)
String name = user.name();      // getName() 아님!
int age = user.age();
String email = user.email();

// 불변이므로 setter 없음
// user.setAge(31);  // ❌ 컴파일 에러

// equals/hashCode 자동
UserDto user2 = new UserDto("John", 30, "john@example.com");
System.out.println(user.equals(user2));  // true

// toString 자동
System.out.println(user);
// UserDto[name=John, age=30, email=john@example.com]
```

### Record의 고급 기능

```java
// 검증 로직 추가
public record Email(String value) {
    public Email {  // Compact Constructor
        if (value == null || !value.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + value);
        }
        value = value.toLowerCase();  // 정규화
    }
}

// 계산된 메서드 추가
public record Rectangle(double width, double height) {
    public double area() {
        return width * height;
    }

    public boolean isSquare() {
        return width == height;
    }
}

// 정적 팩토리 메서드
public record User(String name, int age) {
    public static User of(String name, int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age must be positive");
        }
        return new User(name, age);
    }
}
```

### Record vs Class 선택 기준

**Record를 사용할 때:**
- ✅ 순수 데이터 전송 객체 (DTO)
- ✅ API 응답/요청 모델
- ✅ 불변 값 객체 (Value Object)
- ✅ Configuration 데이터

```java
public record CreateUserRequest(String name, String email) {}
public record UserResponse(Long id, String name, String email) {}
public record Money(BigDecimal amount, Currency currency) {}
public record Point(int x, int y) {}
```

**Class를 사용할 때:**
- ✅ 상태가 변경되는 도메인 엔티티
- ✅ 복잡한 비즈니스 로직
- ✅ 상속이 필요한 경우
- ✅ JPA Entity (Record는 불가능)

```java
public class Order {  // 상태 변경이 필요
    private OrderStatus status;

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}
```

---

## Optional - 올바른 null 처리

### 안티패턴: Optional 남용

```java
// ❌ 잘못된 사용
public Optional<User> findUser(Long id) {
    User user = userRepository.findById(id);
    if (user != null) {
        return Optional.of(user);
    } else {
        return Optional.empty();
    }
}

// 사용처
Optional<User> userOpt = findUser(1L);
if (userOpt.isPresent()) {
    User user = userOpt.get();
    System.out.println(user.getName());
}
// 이러면 그냥 null 체크랑 뭐가 다름?
```

### 올바른 Optional 사용

```java
// ✅ 반환 타입으로만 사용
public Optional<User> findUser(Long id) {
    return userRepository.findById(id);  // Repository가 Optional 반환
}

// ✅ 함수형 체이닝
findUser(1L)
    .map(User::getName)
    .map(String::toUpperCase)
    .ifPresent(System.out::println);

// ✅ orElse, orElseGet
String name = findUser(1L)
    .map(User::getName)
    .orElse("Unknown");

User user = findUser(1L)
    .orElseGet(() -> createDefaultUser());

// ✅ orElseThrow
User user = findUser(1L)
    .orElseThrow(() -> new UserNotFoundException(1L));
```

### Optional 사용 원칙

**DO ✅:**
- 메서드 반환 타입으로 사용
- 함수형 메서드 체이닝
- orElse, orElseGet, orElseThrow

```java
public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

String displayName = findByEmail("john@example.com")
    .map(User::getName)
    .orElse("Guest");
```

**DON'T ❌:**
- 필드로 사용하지 말 것
- 메서드 파라미터로 사용하지 말 것
- 컬렉션을 Optional로 감싸지 말 것
- isPresent() + get() 조합 (null 체크와 동일)

```java
// ❌ 안티패턴
public class User {
    private Optional<String> middleName;  // 필드로 사용 X
}

public void updateUser(Optional<String> name) {  // 파라미터로 사용 X
    // ...
}

public Optional<List<User>> findUsers() {  // 컬렉션은 빈 리스트 반환
    return Optional.of(users);  // ❌
}

// ✅ 올바른 방법
public class User {
    private String middleName;  // null 허용
}

public void updateUser(String name) {  // nullable 파라미터
    // ...
}

public List<User> findUsers() {  // 빈 리스트 반환
    return users.isEmpty() ? List.of() : users;
}
```

### Optional 실전 패턴

```java
// 패턴 1: Optional 체이닝
public Optional<String> getUserCity(Long userId) {
    return findUser(userId)
        .flatMap(User::getAddress)        // Optional<Address>
        .flatMap(Address::getCity)        // Optional<City>
        .map(City::getName);              // Optional<String>
}

// 패턴 2: 조건부 처리
findUser(userId).ifPresentOrElse(
    user -> sendEmail(user),              // 존재하면
    () -> log.warn("User not found")      // 없으면
);

// 패턴 3: Optional.filter
findUser(userId)
    .filter(User::isActive)
    .ifPresent(user -> grantAccess(user));

// 패턴 4: orElse vs orElseGet
// ❌ orElse: 항상 실행됨 (성능 낭비)
String name = optional.orElse(expensiveOperation());

// ✅ orElseGet: 필요할 때만 실행
String name = optional.orElseGet(() -> expensiveOperation());
```

---

## Stream API - 함수형 데이터 처리

### 기본 패턴

```java
List<User> users = getUsers();

// ✅ 필터링
List<User> activeUsers = users.stream()
    .filter(User::isActive)
    .toList();  // Java 16+, 이전 버전은 .collect(Collectors.toList())

// ✅ 변환
List<String> names = users.stream()
    .map(User::getName)
    .toList();

// ✅ 정렬
List<User> sorted = users.stream()
    .sorted(Comparator.comparing(User::getName))
    .toList();

// ✅ 제한
List<User> top10 = users.stream()
    .limit(10)
    .toList();
```

### 실전 패턴

```java
// 패턴 1: 그룹핑
Map<String, List<User>> usersByCity = users.stream()
    .collect(Collectors.groupingBy(User::getCity));

// 패턴 2: 합계/평균
double averageAge = users.stream()
    .mapToInt(User::getAge)
    .average()
    .orElse(0.0);

BigDecimal totalAmount = orders.stream()
    .map(Order::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// 패턴 3: 조건 체크
boolean hasAdmin = users.stream()
    .anyMatch(user -> user.getRole() == Role.ADMIN);

boolean allActive = users.stream()
    .allMatch(User::isActive);

// 패턴 4: 첫 번째 찾기
Optional<User> firstAdmin = users.stream()
    .filter(user -> user.getRole() == Role.ADMIN)
    .findFirst();

// 패턴 5: flatMap (중첩 구조 펼치기)
List<Order> allOrders = users.stream()
    .flatMap(user -> user.getOrders().stream())
    .toList();

// 패턴 6: distinct
List<String> uniqueCities = users.stream()
    .map(User::getCity)
    .distinct()
    .toList();
```

### Stream 성능 고려사항

```java
// ❌ 작은 컬렉션에 Stream은 오버헤드
List<Integer> numbers = List.of(1, 2, 3, 4, 5);
int sum = numbers.stream().mapToInt(Integer::intValue).sum();

// ✅ 간단한 for문이 더 빠름
int sum = 0;
for (int num : numbers) {
    sum += num;
}

// ✅ Stream이 유용한 경우: 복잡한 변환 파이프라인
Map<String, List<OrderDto>> result = orders.stream()
    .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
    .map(order -> new OrderDto(
        order.getId(),
        order.getCustomer().getName(),
        order.getTotal()
    ))
    .collect(Collectors.groupingBy(OrderDto::customerName));
```

### Parallel Stream 주의사항

```java
// ✅ CPU 집약적 작업에 유용
List<BigDecimal> results = hugeList.parallelStream()
    .map(this::expensiveCalculation)
    .toList();

// ❌ I/O 작업에는 비효율적
List<User> users = userIds.parallelStream()
    .map(userRepository::findById)  // DB 조회 - 병렬 처리 비효율
    .toList();

// ✅ I/O는 비동기 방식이 낫다
CompletableFuture<List<User>> futureUsers = CompletableFuture.supplyAsync(
    () -> userRepository.findAllById(userIds)
);
```

---

## 정적 팩토리 메서드

### 생성자 vs 정적 팩토리

```java
// ❌ 생성자: 의미 불명확
LocalDate date1 = new LocalDate(2024, 1, 15);  // 뭐가 뭐지?

// ✅ 정적 팩토리: 의미 명확
LocalDate date2 = LocalDate.of(2024, 1, 15);
LocalDate now = LocalDate.now();
LocalDate parsed = LocalDate.parse("2024-01-15");
```

### 실전 패턴

```java
public class Money {
    private final BigDecimal amount;
    private final Currency currency;

    // private 생성자
    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    // ✅ 다양한 생성 방법
    public static Money of(BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return new Money(amount, currency);
    }

    public static Money krw(long amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("KRW"));
    }

    public static Money usd(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("USD"));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money parse(String text) {
        // "1000 KRW" 파싱
        String[] parts = text.split(" ");
        return new Money(
            new BigDecimal(parts[0]),
            Currency.getInstance(parts[1])
        );
    }
}

// 사용
Money price1 = Money.krw(10000);
Money price2 = Money.usd(99.99);
Money zero = Money.zero(Currency.getInstance("KRW"));
```

### 장점

1. **이름을 가질 수 있다**
```java
User.createGuest()  vs  new User("Guest", 0)
User.fromEmail(email)  vs  new User(email)
```

2. **호출될 때마다 새 객체를 생성하지 않아도 된다**
```java
public class Boolean {
    public static final Boolean TRUE = new Boolean(true);
    public static final Boolean FALSE = new Boolean(false);

    public static Boolean valueOf(boolean b) {
        return b ? TRUE : FALSE;  // 캐싱
    }
}
```

3. **반환 타입의 하위 타입을 반환할 수 있다**
```java
public interface Payment {
    static Payment create(PaymentType type) {
        return switch (type) {
            case CARD -> new CardPayment();
            case BANK -> new BankTransferPayment();
            case MOBILE -> new MobilePayment();
        };
    }
}
```

4. **입력 매개변수에 따라 다른 클래스의 객체를 반환할 수 있다**
```java
public static <T> List<T> of(T... elements) {
    return switch (elements.length) {
        case 0 -> ImmutableCollections.emptyList();
        case 1 -> new ImmutableCollections.List12<>(elements[0]);
        default -> new ImmutableCollections.ListN<>(elements);
    };
}
```

---

## Result 패턴 - Error as Value

### 문제: 예외로 흐름 제어

```java
// ❌ 예외로 비즈니스 로직 제어
public User createUser(String email, String password) {
    if (!EmailValidator.isValid(email)) {
        throw new InvalidEmailException(email);
    }

    if (userRepository.existsByEmail(email)) {
        throw new DuplicateEmailException(email);
    }

    if (!PasswordPolicy.isStrong(password)) {
        throw new WeakPasswordException();
    }

    return userRepository.save(new User(email, password));
}

// 호출처에서 예외 처리 지옥
try {
    User user = userService.createUser(email, password);
    return ResponseEntity.ok(user);
} catch (InvalidEmailException e) {
    return ResponseEntity.badRequest().body("Invalid email");
} catch (DuplicateEmailException e) {
    return ResponseEntity.status(409).body("Email already exists");
} catch (WeakPasswordException e) {
    return ResponseEntity.badRequest().body("Weak password");
}
```

### 해결: Result<T> 패턴

```java
// ✅ Result 타입 정의
public sealed interface Result<T> {
    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(Error error) implements Result<T> {}

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(Error error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    default T getValue() {
        return switch (this) {
            case Success<T> s -> s.value();
            case Failure<T> f -> throw new IllegalStateException("No value");
        };
    }

    default Error getError() {
        return switch (this) {
            case Success<T> s -> throw new IllegalStateException("No error");
            case Failure<T> f -> f.error();
        };
    }

    // 함수형 메서드
    default <U> Result<U> map(Function<T, U> mapper) {
        return switch (this) {
            case Success<T> s -> success(mapper.apply(s.value()));
            case Failure<T> f -> failure(f.error());
        };
    }

    default <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        return switch (this) {
            case Success<T> s -> mapper.apply(s.value());
            case Failure<T> f -> failure(f.error());
        };
    }
}

public record Error(String code, String message) {}
```

### 실전 사용

```java
// ✅ 명시적 에러 처리
public Result<User> createUser(String email, String password) {
    if (!EmailValidator.isValid(email)) {
        return Result.failure(new Error("INVALID_EMAIL", "유효하지 않은 이메일"));
    }

    if (userRepository.existsByEmail(email)) {
        return Result.failure(new Error("DUPLICATE_EMAIL", "이미 존재하는 이메일"));
    }

    if (!PasswordPolicy.isStrong(password)) {
        return Result.failure(new Error("WEAK_PASSWORD", "비밀번호가 너무 약함"));
    }

    User user = userRepository.save(new User(email, password));
    return Result.success(user);
}

// 호출처 - 명확한 에러 처리
Result<User> result = userService.createUser(email, password);

return switch (result) {
    case Result.Success<User> s -> ResponseEntity.ok(s.value());
    case Result.Failure<User> f -> switch (f.error().code()) {
        case "INVALID_EMAIL", "WEAK_PASSWORD" ->
            ResponseEntity.badRequest().body(f.error().message());
        case "DUPLICATE_EMAIL" ->
            ResponseEntity.status(409).body(f.error().message());
        default ->
            ResponseEntity.internalServerError().build();
    };
};

// 또는 함수형 체이닝
result
    .map(user -> new UserDto(user.getId(), user.getEmail()))
    .map(ResponseEntity::ok)
    .getOrElse(() -> ResponseEntity.badRequest().build());
```

### Go의 error 패턴과 비교

```go
// Go
func createUser(email, password string) (*User, error) {
    if !isValidEmail(email) {
        return nil, errors.New("invalid email")
    }

    user := &User{Email: email}
    return user, nil
}

// 사용
user, err := createUser(email, password)
if err != nil {
    return err
}
```

```java
// Java with Result
public Result<User> createUser(String email, String password) {
    if (!EmailValidator.isValid(email)) {
        return Result.failure(new Error("INVALID_EMAIL", "invalid email"));
    }

    return Result.success(new User(email));
}

// 사용
Result<User> result = createUser(email, password);
if (result.isFailure()) {
    return result.getError();
}
```

---

## Pattern Matching과 Sealed Classes

### Sealed Classes (Java 17+)

```java
// ✅ 제한된 하위 타입
public sealed interface Payment
    permits CreditCardPayment, BankTransferPayment, MobilePayment {

    Money getAmount();
}

public final class CreditCardPayment implements Payment {
    private final String cardNumber;
    private final Money amount;

    @Override
    public Money getAmount() { return amount; }
}

public final class BankTransferPayment implements Payment {
    private final String accountNumber;
    private final Money amount;

    @Override
    public Money getAmount() { return amount; }
}

public final class MobilePayment implements Payment {
    private final String phoneNumber;
    private final Money amount;

    @Override
    public Money getAmount() { return amount; }
}
```

### Pattern Matching (Java 21+)

```java
// ✅ Switch 표현식 + 패턴 매칭
public String processPayment(Payment payment) {
    return switch (payment) {
        case CreditCardPayment card ->
            "카드 결제: " + maskCardNumber(card.cardNumber());
        case BankTransferPayment bank ->
            "계좌 이체: " + bank.accountNumber();
        case MobilePayment mobile ->
            "모바일 결제: " + mobile.phoneNumber();
        // sealed이므로 모든 케이스 커버 = default 불필요
    };
}

// Record 패턴 (Java 21+)
public double calculateDiscount(Payment payment) {
    return switch (payment) {
        case CreditCardPayment(var number, Money(var amount, var currency))
            when amount.compareTo(new BigDecimal("100000")) > 0 ->
            amount.multiply(new BigDecimal("0.1")).doubleValue();
        case BankTransferPayment(var account, var amount) ->
            0.0;  // 계좌 이체는 할인 없음
        case MobilePayment mp ->
            mp.getAmount().multiply(new BigDecimal("0.05")).doubleValue();
    };
}
```

### 실전 예시: 상태 머신

```java
public sealed interface OrderState
    permits Pending, Confirmed, Shipped, Delivered, Cancelled {
}

public record Pending() implements OrderState {}
public record Confirmed(LocalDateTime confirmedAt) implements OrderState {}
public record Shipped(String trackingNumber) implements OrderState {}
public record Delivered(LocalDateTime deliveredAt) implements OrderState {}
public record Cancelled(String reason) implements OrderState {}

public class Order {
    private OrderState state;

    public Result<Void> confirm() {
        return switch (state) {
            case Pending p -> {
                state = new Confirmed(LocalDateTime.now());
                yield Result.success(null);
            }
            case Confirmed c ->
                Result.failure(new Error("ALREADY_CONFIRMED", "이미 확인됨"));
            case Shipped s, Delivered d, Cancelled c ->
                Result.failure(new Error("INVALID_STATE", "확인할 수 없는 상태"));
        };
    }

    public String getStatusMessage() {
        return switch (state) {
            case Pending p -> "주문 대기 중";
            case Confirmed(var confirmedAt) ->
                "주문 확인됨 (%s)".formatted(confirmedAt);
            case Shipped(var trackingNumber) ->
                "배송 중 (송장: %s)".formatted(trackingNumber);
            case Delivered(var deliveredAt) ->
                "배송 완료 (%s)".formatted(deliveredAt);
            case Cancelled(var reason) ->
                "취소됨: %s".formatted(reason);
        };
    }
}
```

---

## 요약

| 기능 | 사용 시기 | 장점 |
|------|----------|------|
| **Record** | DTO, 값 객체, 불변 데이터 | 보일러플레이트 제거, 불변성 |
| **Optional** | null 가능한 반환값 | 명시적 null 처리, 함수형 체이닝 |
| **Stream** | 복잡한 컬렉션 변환 | 선언적 코드, 가독성 |
| **정적 팩토리** | 객체 생성 | 명확한 의미, 캐싱, 유연성 |
| **Result<T>** | 비즈니스 에러 처리 | 명시적 에러, 타입 안전 |
| **Sealed Classes** | 제한된 상속 | 완전한 패턴 매칭, 타입 안전 |

**핵심 원칙:**
- 불변성을 기본으로
- 예외는 예외적인 상황에만
- 타입 시스템 활용
- 함수형 접근 고려

---

**이전**: ← [안티패턴 피하기](./01-anti-patterns.md)
**다음**: [실용적인 코드 구조](./03-code-structure.md) →
