# 안티패턴 피하기

> 실무에서 자주 보는 잘못된 패턴과 해결책

## 목차
- [과도한 OOP](#과도한-oop)
- [getter/setter 안티패턴](#gettersetter-안티패턴)
- [불필요한 인터페이스와 추상화](#불필요한-인터페이스와-추상화)
- [God Class와 Anemic Domain Model](#god-class와-anemic-domain-model)
- [성능 안티패턴](#성능-안티패턴)

---

## 과도한 OOP

### 문제: 모든 것을 객체로 만들기

```java
// ❌ 안티패턴: 단순한 유틸리티도 클래스로
public class StringUtility {
    private String value;

    public StringUtility(String value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    public String toUpperCase() {
        return value.toUpperCase();
    }
}

// 사용
StringUtility utility = new StringUtility("hello");
if (!utility.isEmpty()) {
    System.out.println(utility.toUpperCase());
}
```

```java
// ✅ 해결책: 정적 메서드 사용
public class StringUtils {
    private StringUtils() {} // 인스턴스 생성 방지

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String toUpperCase(String str) {
        return str != null ? str.toUpperCase() : null;
    }
}

// 사용
String text = "hello";
if (!StringUtils.isEmpty(text)) {
    System.out.println(StringUtils.toUpperCase(text));
}
```

**Go와 비교:**
```go
// Go는 이런 식으로 간단
func isEmpty(s string) bool {
    return len(s) == 0
}

if !isEmpty(text) {
    fmt.Println(strings.ToUpper(text))
}
```

### 문제: 과도한 계층 구조

```java
// ❌ 안티패턴: 불필요한 추상화 계층
public interface Animal {
    void makeSound();
}

public abstract class AbstractAnimal implements Animal {
    protected String name;

    public AbstractAnimal(String name) {
        this.name = name;
    }

    public abstract void makeSound();
}

public abstract class Mammal extends AbstractAnimal {
    public Mammal(String name) {
        super(name);
    }
}

public class Dog extends Mammal {
    public Dog(String name) {
        super(name);
    }

    @Override
    public void makeSound() {
        System.out.println(name + " barks");
    }
}

// 단순히 소리만 내는데 4개 레이어!
```

```java
// ✅ 해결책: 필요한 만큼만
public class Dog {
    private final String name;

    public Dog(String name) {
        this.name = name;
    }

    public void bark() {
        System.out.println(name + " barks");
    }
}

// 나중에 진짜 필요할 때 인터페이스 추가
```

**원칙**: YAGNI (You Aren't Gonna Need It)
- 지금 필요한 기능만 구현
- 미래를 위한 과도한 설계는 독

---

## getter/setter 안티패턴

### 문제: 의미없는 getter/setter

```java
// ❌ 안티패턴: 그냥 필드 노출과 다를 바 없음
public class User {
    private String name;
    private int age;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

// 사용
user.setAge(-5);  // ❌ 검증 없음
user.setName("");  // ❌ 빈 문자열 허용
```

```java
// ✅ 해결책 1: Record 사용 (단순 데이터 홀더)
public record UserDto(String name, int age) {}

// 불변이고 간결함
```

```java
// ✅ 해결책 2: 도메인 메서드 (비즈니스 로직 있을 때)
public class User {
    private String name;
    private int age;

    private User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // 정적 팩토리로 검증
    public static Result<User> create(String name, int age) {
        if (name == null || name.isBlank()) {
            return Result.failure(new ValidationError("이름은 필수입니다"));
        }
        if (age < 0 || age > 150) {
            return Result.failure(new ValidationError("나이가 유효하지 않습니다"));
        }
        return Result.success(new User(name, age));
    }

    // getter만 (setter 없음)
    public String getName() { return name; }
    public int getAge() { return age; }

    // 도메인 메서드
    public boolean isAdult() {
        return age >= 18;
    }
}
```

### 문제: Private의 무의미함

```java
// ❌ 이게 캡슐화인가?
public class BankAccount {
    private BigDecimal balance;

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;  // 검증 없음
    }
}

// 누구나 마음대로 수정 가능
account.setBalance(new BigDecimal("-99999"));
```

```java
// ✅ 진짜 캡슐화
public class BankAccount {
    private Money balance;  // Money는 불변 값 객체

    public Money getBalance() {
        return balance;  // Money가 불변이므로 안전
    }

    // setter 없음! 도메인 메서드로만 변경
    public Result<Void> withdraw(Money amount) {
        if (balance.isLessThan(amount)) {
            return Result.failure(new InsufficientBalanceError());
        }
        this.balance = balance.subtract(amount);
        return Result.success(null);
    }

    public void deposit(Money amount) {
        if (amount.isNegativeOrZero()) {
            throw new IllegalArgumentException("입금액은 양수여야 합니다");
        }
        this.balance = balance.add(amount);
    }
}
```

**핵심**:
- DTO → Record 사용
- 도메인 모델 → setter 없이 도메인 메서드
- private은 setter가 없을 때만 의미있음

---

## 불필요한 인터페이스와 추상화

### 문제: 구현체가 하나뿐인 인터페이스

```java
// ❌ 안티패턴: 쓸모없는 인터페이스
public interface UserService {
    User findById(Long id);
    void save(User user);
}

public class UserServiceImpl implements UserService {
    @Override
    public User findById(Long id) { ... }

    @Override
    public void save(User user) { ... }
}

// 평생 UserServiceImpl 하나만 쓸 건데 왜 인터페이스를?
```

```java
// ✅ 해결책: 구체 클래스만 사용
@Service
public class UserService {
    public User findById(Long id) { ... }
    public void save(User user) { ... }
}

// 나중에 진짜 여러 구현체가 필요하면 그때 인터페이스 추출
```

**언제 인터페이스를 만들어야 하나?**

✅ **좋은 이유:**
- 여러 구현체가 실제로 필요할 때 (PaymentGateway: Stripe, PayPal, Toss)
- 테스트를 위한 Mock이 필요할 때 (외부 API 호출)
- 전략 패턴 등 명확한 다형성이 필요할 때

❌ **나쁜 이유:**
- "나중에 필요할 수도 있으니까" (YAGNI 위반)
- "인터페이스가 좋은 습관이라고 들어서"
- "Spring에서 @Autowired 하려면 필요해서" (아님)

### 문제: 과도한 추상화

```java
// ❌ 안티패턴: 읽기 어려운 추상화
public interface DataSource<T> {
    T fetch(Query query);
}

public interface QueryBuilder<T> {
    Query build(T criteria);
}

public interface Repository<T, ID> {
    Optional<T> findById(ID id);
}

public class UserRepository implements Repository<User, Long> {
    private final DataSource<User> dataSource;
    private final QueryBuilder<UserCriteria> queryBuilder;

    // 코드가 복잡해져서 읽기 힘듦
}
```

```java
// ✅ 해결책: 구체적이고 명확하게
@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbc.query(sql, userRowMapper, id)
                   .stream()
                   .findFirst();
    }

    public List<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbc.query(sql, userRowMapper, email);
    }
}

// 읽기 쉽고, 디버깅 쉽고, 유지보수 쉬움
```

**Go의 교훈:**
```go
// Go는 작은 인터페이스를 선호
type Reader interface {
    Read(p []byte) (n int, err error)
}

// 메서드 1-3개 정도의 작은 인터페이스
// Java도 이렇게 할 수 있음
```

---

## God Class와 Anemic Domain Model

### 안티패턴 1: God Class (모든 걸 아는 클래스)

```java
// ❌ 안티패턴: 1000줄짜리 괴물 클래스
@Service
public class UserService {
    // 사용자 관리
    public void createUser(...) { }
    public void updateUser(...) { }
    public void deleteUser(...) { }

    // 인증
    public void login(...) { }
    public void logout(...) { }
    public void resetPassword(...) { }

    // 이메일
    public void sendWelcomeEmail(...) { }
    public void sendPasswordResetEmail(...) { }

    // 알림
    public void sendPushNotification(...) { }

    // 통계
    public UserStatistics calculateStatistics(...) { }

    // 결제
    public void processSubscription(...) { }

    // ... 계속 증가
}
```

```java
// ✅ 해결책: 책임에 따라 분리
@Service
public class UserService {
    public void createUser(...) { }
    public void updateUser(...) { }
    public User findById(Long id) { }
}

@Service
public class AuthenticationService {
    public void login(...) { }
    public void logout(...) { }
    public void resetPassword(...) { }
}

@Service
public class NotificationService {
    public void sendEmail(...) { }
    public void sendPush(...) { }
}

@Service
public class UserStatisticsService {
    public UserStatistics calculate(Long userId) { }
}
```

### 안티패턴 2: Anemic Domain Model (빈혈 도메인 모델)

```java
// ❌ 안티패턴: 로직이 없는 데이터 덩어리
public class Order {
    private Long id;
    private List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;

    // getter/setter만 잔뜩
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ...
}

@Service
public class OrderService {
    // 모든 비즈니스 로직이 서비스에
    public void calculateTotal(Order order) {
        BigDecimal total = order.getItems().stream()
            .map(item -> item.getPrice().multiply(item.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
    }

    public boolean canCancel(Order order) {
        return order.getStatus() == OrderStatus.PENDING
            || order.getStatus() == OrderStatus.CONFIRMED;
    }

    public void cancel(Order order) {
        if (!canCancel(order)) {
            throw new IllegalStateException();
        }
        order.setStatus(OrderStatus.CANCELLED);
    }
}
```

```java
// ✅ 해결책: Rich Domain Model
public class Order {
    private final Long id;
    private final List<OrderItem> items;
    private OrderStatus status;

    private Order(Long id, List<OrderItem> items) {
        this.id = id;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
    }

    public static Result<Order> create(List<OrderItem> items) {
        if (items.isEmpty()) {
            return Result.failure(new ValidationError("주문 항목이 없습니다"));
        }
        return Result.success(new Order(null, items));
    }

    // 도메인 로직이 객체 안에
    public Money calculateTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    public boolean canCancel() {
        return status == OrderStatus.PENDING
            || status == OrderStatus.CONFIRMED;
    }

    public Result<Void> cancel() {
        if (!canCancel()) {
            return Result.failure(
                new InvalidStateError("취소할 수 없는 상태입니다")
            );
        }
        this.status = OrderStatus.CANCELLED;
        return Result.success(null);
    }

    // getter만 (setter 없음)
    public Money getTotal() { return calculateTotal(); }
    public OrderStatus getStatus() { return status; }
}

@Service
public class OrderService {
    // 서비스는 조율만
    @Transactional
    public Result<Order> cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        Result<Void> cancelResult = order.cancel();
        if (cancelResult.isFailure()) {
            return Result.failure(cancelResult.getError());
        }

        orderRepository.save(order);
        eventPublisher.publish(new OrderCancelledEvent(orderId));

        return Result.success(order);
    }
}
```

**핵심 차이:**
- Anemic: 데이터와 로직 분리 → 객체가 데이터 컨테이너에 불과
- Rich: 데이터와 로직 함께 → 진짜 객체지향

---

## 성능 안티패턴

### 안티패턴 1: N+1 쿼리

```java
// ❌ 안티패턴: 1 + N번의 쿼리 실행
@Service
public class PostService {
    public List<PostDto> getAllPosts() {
        List<Post> posts = postRepository.findAll();  // 1번

        return posts.stream()
            .map(post -> {
                User author = userRepository.findById(post.getAuthorId()).get();  // N번!
                return new PostDto(post.getTitle(), author.getName());
            })
            .toList();
    }
}
```

```java
// ✅ 해결책: Join Fetch 또는 DTO Projection
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p JOIN FETCH p.author")
    List<Post> findAllWithAuthor();
}

@Service
public class PostService {
    public List<PostDto> getAllPosts() {
        return postRepository.findAllWithAuthor()  // 1번의 JOIN 쿼리
            .stream()
            .map(post -> new PostDto(post.getTitle(), post.getAuthor().getName()))
            .toList();
    }
}
```

### 안티패턴 2: 불필요한 객체 생성

```java
// ❌ 안티패턴: 반복문에서 객체 생성
public String concatenate(List<String> words) {
    String result = "";
    for (String word : words) {
        result = result + word;  // 매번 새 String 객체 생성!
    }
    return result;
}
```

```java
// ✅ 해결책: StringBuilder 사용
public String concatenate(List<String> words) {
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
        sb.append(word);
    }
    return sb.toString();
}

// 또는 Stream
public String concatenate(List<String> words) {
    return String.join("", words);
}
```

### 안티패턴 3: 과도한 Stream 사용

```java
// ❌ 안티패턴: 작은 컬렉션에 Stream은 오버헤드
public boolean hasAdmin(List<User> users) {
    return users.stream()
        .filter(user -> user.getRole() == Role.ADMIN)
        .findFirst()
        .isPresent();
}
```

```java
// ✅ 해결책: 간단한 반복문이 더 빠름
public boolean hasAdmin(List<User> users) {
    for (User user : users) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
    }
    return false;
}
```

**Stream을 쓸 때:**
- ✅ 대용량 데이터 처리
- ✅ 복잡한 변환 파이프라인
- ✅ Parallel Stream으로 병렬 처리

**Stream을 피할 때:**
- ❌ 작은 컬렉션 (< 100개)
- ❌ 단순한 반복문
- ❌ 성능이 중요한 핫 패스

---

## 요약: 피해야 할 것들

| 안티패턴 | 문제 | 해결책 |
|---------|------|--------|
| 과도한 OOP | 불필요한 객체화, 복잡한 계층 | YAGNI, 단순하게 |
| 의미없는 getter/setter | private의 무의미함 | Record 또는 도메인 메서드 |
| 불필요한 인터페이스 | 읽기 어려운 코드 | 필요할 때만 추상화 |
| God Class | 모든 책임을 가진 클래스 | 단일 책임 원칙 |
| Anemic Model | 로직 없는 데이터 객체 | Rich Domain Model |
| N+1 쿼리 | 성능 문제 | Join Fetch, DTO Projection |
| 불필요한 Stream | 오버헤드 | 간단한 for문 |

**기억하세요:**
- 단순함이 복잡함을 이긴다
- 나중을 위한 설계는 독이다 (YAGNI)
- 성능은 측정 후 최적화하라
- 코드는 읽히기 위해 존재한다

---

**다음**: [현대적 Java 기능 활용](./02-modern-java.md) →
