# 협업 Best Practices

> 팀에서 함께 일하기 위한 코딩 습관

## 목차
- [읽기 좋은 코드 작성](#읽기-좋은-코드-작성)
- [네이밍 컨벤션](#네이밍-컨벤션)
- [주석 vs 자기문서화 코드](#주석-vs-자기문서화-코드)
- [코드 리뷰 가이드](#코드-리뷰-가이드)
- [Git 워크플로우](#git-워크플로우)

---

## 읽기 좋은 코드 작성

### 원칙: 코드는 한 번 쓰고 열 번 읽힌다

```java
// ❌ 나쁜 예: 무슨 일을 하는지 알기 어려움
public List<User> get(int s) {
    List<User> r = new ArrayList<>();
    for (User u : users) {
        if (u.getStatus() == s && u.getCreatedAt().isAfter(
            LocalDateTime.now().minusDays(30))) {
            r.add(u);
        }
    }
    return r;
}

// ✅ 좋은 예: 의도가 명확함
public List<User> findActiveUsersCreatedInLast30Days(UserStatus status) {
    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

    return users.stream()
        .filter(user -> user.hasStatus(status))
        .filter(user -> user.wasCreatedAfter(thirtyDaysAgo))
        .toList();
}
```

### 함수는 한 가지 일만

```java
// ❌ 안티패턴: 너무 많은 일을 함
public void processOrder(Order order) {
    // 검증
    if (order.getItems().isEmpty()) {
        throw new IllegalArgumentException();
    }

    // 재고 확인
    for (OrderItem item : order.getItems()) {
        if (inventory.getStock(item.getProductId()) < item.getQuantity()) {
            throw new OutOfStockException();
        }
    }

    // 결제
    Payment payment = paymentGateway.charge(order.getTotal());

    // 재고 차감
    for (OrderItem item : order.getItems()) {
        inventory.decrease(item.getProductId(), item.getQuantity());
    }

    // 이메일 발송
    emailService.send(order.getCustomer().getEmail(), "주문 완료");

    // 로깅
    logger.info("Order processed: {}", order.getId());

    // 통계
    statistics.increment("order.count");
}

// ✅ 해결책: 역할별로 분리
public void processOrder(Order order) {
    validateOrder(order);
    checkInventory(order);
    Payment payment = processPayment(order);
    decreaseInventory(order);
    notifyCustomer(order);
    recordStatistics(order);
}

private void validateOrder(Order order) {
    if (order.getItems().isEmpty()) {
        throw new IllegalArgumentException("주문 항목이 없습니다");
    }
}

private void checkInventory(Order order) {
    for (OrderItem item : order.getItems()) {
        if (!inventory.hasStock(item.getProductId(), item.getQuantity())) {
            throw new OutOfStockException(item.getProductId());
        }
    }
}

private Payment processPayment(Order order) {
    return paymentGateway.charge(order.getTotal());
}

// ... 나머지 메서드
```

### Early Return으로 중첩 줄이기

```java
// ❌ 중첩이 깊음
public void updateUser(Long userId, UpdateUserRequest request) {
    User user = userRepository.findById(userId).orElse(null);
    if (user != null) {
        if (user.isActive()) {
            if (request.getName() != null) {
                if (request.getName().length() > 0) {
                    user.setName(request.getName());
                    userRepository.save(user);
                }
            }
        }
    }
}

// ✅ Early Return
public void updateUser(Long userId, UpdateUserRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    if (!user.isActive()) {
        throw new InactiveUserException(userId);
    }

    if (request.getName() == null || request.getName().isBlank()) {
        throw new IllegalArgumentException("이름은 필수입니다");
    }

    user.setName(request.getName());
    userRepository.save(user);
}
```

### 매직 넘버/문자열 제거

```java
// ❌ 매직 넘버
public boolean canWithdraw(BigDecimal amount) {
    return balance.compareTo(amount) >= 0
        && amount.compareTo(new BigDecimal("10000000")) <= 0
        && dailyWithdrawal.compareTo(new BigDecimal("5000000")) <= 0;
}

// ✅ 상수로 명확하게
public class WithdrawalPolicy {
    private static final BigDecimal MAX_SINGLE_WITHDRAWAL = new BigDecimal("10000000");
    private static final BigDecimal MAX_DAILY_WITHDRAWAL = new BigDecimal("5000000");

    public boolean canWithdraw(
            BigDecimal balance,
            BigDecimal amount,
            BigDecimal dailyWithdrawal) {
        return balance.compareTo(amount) >= 0
            && amount.compareTo(MAX_SINGLE_WITHDRAWAL) <= 0
            && dailyWithdrawal.compareTo(MAX_DAILY_WITHDRAWAL) <= 0;
    }
}
```

### Boolean 파라미터 피하기

```java
// ❌ 호출부에서 의미 불명
service.createUser("john@example.com", true, false, true);

// ✅ 명시적인 메서드 또는 객체
service.createActiveUser("john@example.com");
service.createUser(new UserCreationOptions(
    email: "john@example.com",
    sendWelcomeEmail: true,
    verifyEmail: false,
    enableNotifications: true
));

// 또는 Builder 패턴
User user = User.builder()
    .email("john@example.com")
    .active(true)
    .sendWelcomeEmail(false)
    .enableNotifications(true)
    .build();
```

---

## 네이밍 컨벤션

### 클래스명

```java
// ✅ 명사 또는 명사구
public class User { }
public class OrderService { }
public class PaymentGateway { }
public class EmailSender { }

// ❌ 동사 사용 금지
public class ProcessOrder { }  // ❌
public class SendEmail { }     // ❌
```

### 메서드명

```java
// ✅ 동사로 시작
public void createOrder() { }
public User findById(Long id) { }
public boolean isActive() { }
public boolean hasPermission() { }
public void sendEmail() { }

// ✅ get/set은 단순 접근자에만
public String getName() { return name; }
public void setName(String name) { this.name = name; }

// ✅ 계산이나 로직이 있으면 calculate, compute
public Money calculateTotal() {
    return items.stream()
        .map(OrderItem::getSubtotal)
        .reduce(Money.ZERO, Money::add);
}

// ❌ get으로 무거운 연산 숨기기
public BigDecimal getTotal() {
    // 데이터베이스 조회?? ❌
    return orderRepository.calculateTotal(this.id);
}
```

### Boolean 변수/메서드

```java
// ✅ is, has, can, should로 시작
public boolean isActive() { }
public boolean hasPermission() { }
public boolean canWithdraw() { }
public boolean shouldNotify() { }

// ❌ 애매한 이름
public boolean active() { }     // ❌
public boolean permission() { } // ❌
```

### 컬렉션 네이밍

```java
// ✅ 복수형 사용
List<User> users;
Set<String> userIds;
Map<String, Order> orderMap;

// ✅ 명확한 의미
List<User> activeUsers;
List<Order> pendingOrders;
Set<Long> processedOrderIds;

// ❌ 모호한 이름
List<User> list;     // ❌
Set<String> data;    // ❌
Map<String, Order> map; // ❌
```

### DTO/Request/Response 네이밍

```java
// ✅ 명확한 접미사
public record CreateUserRequest(String email, String name) { }
public record UserResponse(Long id, String name) { }
public record OrderDto(Long id, BigDecimal total) { }

// ✅ 목적이 명확한 DTO
public record UserProfileDto(String name, String bio, String avatar) { }
public record OrderSummaryDto(Long id, int itemCount, BigDecimal total) { }
```

### 약어 사용 규칙

```java
// ✅ 널리 알려진 약어는 OK
public class HttpClient { }
public class JsonParser { }
public class UrlValidator { }

// ❌ 팀 내부에서만 통하는 약어
public class UsrMgr { }        // ❌ UserManager
public class OrdPrcSvc { }     // ❌ OrderProcessService

// ✅ 풀어쓰기
public class UserManager { }
public class OrderProcessService { }
```

---

## 주석 vs 자기문서화 코드

### 나쁜 주석

```java
// ❌ 코드가 하는 일을 반복
// 사용자를 생성한다
public void createUser(String email) {
    User user = new User(email);
    userRepository.save(user);
}

// ❌ 오래된 주석 (코드와 불일치)
// userId로 사용자를 조회한다
public User findByEmail(String email) {  // 이메일로 조회하는데?
    return userRepository.findByEmail(email);
}

// ❌ 주석으로 나쁜 코드 설명
// i는 index, u는 user를 의미
for (int i = 0; i < list.size(); i++) {
    User u = list.get(i);
    // ...
}
```

### 좋은 주석

```java
// ✅ Why를 설명 (What이 아니라)
// 외부 API 제한으로 인해 1초에 10번만 호출 가능
@RateLimited(maxCalls = 10, duration = "1s")
public void callExternalApi() {
    // ...
}

// ✅ 알고리즘 설명
/**
 * Luhn 알고리즘을 사용한 카드 번호 검증
 * https://en.wikipedia.org/wiki/Luhn_algorithm
 */
public boolean validateCardNumber(String cardNumber) {
    // ...
}

// ✅ 주의사항
// 주의: 이 메서드는 트랜잭션 밖에서 호출되어야 함
// 이유: 외부 API 호출 시간이 길어 트랜잭션 타임아웃 발생 가능
public void sendNotificationAsync(Order order) {
    // ...
}

// ✅ TODO 주석 (나중에 처리할 것)
// TODO: 성능 개선 필요 - 현재 N+1 쿼리 발생
public List<OrderDto> getAllOrders() {
    // ...
}
```

### 자기문서화 코드

```java
// ❌ 주석에 의존
public void process(User user) {
    // 활성 사용자이고, 이메일 인증이 완료되고, 30일 이내 로그인한 경우
    if (user.getStatus() == 1 && user.getEmailVerified() == true
        && user.getLastLoginAt().isAfter(LocalDateTime.now().minusDays(30))) {
        // ...
    }
}

// ✅ 메서드로 의도 표현
public void process(User user) {
    if (user.isEligibleForPromotion()) {
        // ...
    }
}

// User 클래스
public boolean isEligibleForPromotion() {
    return isActive()
        && isEmailVerified()
        && hasLoggedInRecently();
}

private boolean isActive() {
    return status == UserStatus.ACTIVE;
}

private boolean isEmailVerified() {
    return emailVerified;
}

private boolean hasLoggedInRecently() {
    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
    return lastLoginAt != null && lastLoginAt.isAfter(thirtyDaysAgo);
}
```

### JavaDoc 작성 가이드

```java
/**
 * 주문을 생성하고 결제를 처리합니다.
 *
 * <p>이 메서드는 다음 작업을 수행합니다:
 * <ul>
 *   <li>주문 항목 검증</li>
 *   <li>재고 확인 및 차감</li>
 *   <li>결제 처리</li>
 *   <li>주문 확인 이메일 발송</li>
 * </ul>
 *
 * @param customerId 고객 ID
 * @param items 주문 항목 목록
 * @return 생성된 주문 정보
 * @throws CustomerNotFoundException 고객을 찾을 수 없는 경우
 * @throws OutOfStockException 재고가 부족한 경우
 * @throws PaymentFailedException 결제 실패 시
 */
public Order createOrder(Long customerId, List<OrderItem> items)
    throws CustomerNotFoundException, OutOfStockException, PaymentFailedException {
    // ...
}
```

---

## 코드 리뷰 가이드

### 리뷰어를 위한 가이드

**1. 친절하고 건설적으로**

```
❌ "이 코드 왜 이렇게 짰어요? 다시 하세요."
✅ "이 부분을 Stream API로 바꾸면 더 읽기 쉬울 것 같습니다. 어떻게 생각하시나요?"

❌ "이건 틀렸어요."
✅ "제 생각에는 이렇게 하면 좀 더 명확할 것 같은데, 의견 부탁드립니다."
```

**2. 구체적으로 제안**

```
❌ "성능 문제가 있어요."
✅ "이 부분에서 N+1 쿼리가 발생할 수 있습니다. JOIN FETCH를 사용하면 어떨까요?
    예: @Query("SELECT o FROM Order o JOIN FETCH o.items")"

❌ "네이밍이 애매해요."
✅ "getUserData()보다 getUserProfile()이 더 명확할 것 같습니다."
```

**3. 중요도 표시**

```
[Critical] 보안 이슈: 사용자 입력을 직접 SQL에 사용하고 있습니다. Prepared Statement를 사용해주세요.

[Important] N+1 쿼리 발생: findAll() 후 반복문에서 각각 조회하고 있습니다.

[Nit] 사소한 의견: 변수명을 data보다 userProfile이 더 명확할 것 같습니다.

[Question] 질문: 이 로직을 서비스 레이어가 아닌 도메인에 두신 이유가 있나요?
```

**4. 좋은 코드도 칭찬**

```
✅ "이 부분 Optional 처리 깔끔하네요!"
✅ "테스트 케이스 꼼꼼하게 작성하셨네요. 👍"
✅ "에러 메시지가 명확해서 좋습니다!"
```

### 리뷰이를 위한 가이드

**1. PR 설명 작성**

```markdown
## 변경 사항
- 주문 생성 API 추가
- 재고 확인 로직 개선

## 동기
고객이 주문 시 재고가 부족한 경우가 빈번하게 발생하여,
주문 생성 전에 재고를 확인하는 로직을 추가했습니다.

## 주요 변경 파일
- `OrderService.java`: 재고 확인 로직 추가
- `InventoryService.java`: 동시성 처리 개선
- `OrderController.java`: 새 API 엔드포인트

## 테스트
- 단위 테스트: OrderServiceTest 추가
- 통합 테스트: 재고 부족 시나리오 테스트
- 수동 테스트: Postman으로 API 검증 완료

## 리뷰 포인트
특히 `InventoryService`의 동시성 처리 부분 검토 부탁드립니다.
```

**2. 작은 PR 만들기**

```
❌ 나쁜 PR:
- 15개 파일 변경
- 1000줄 추가
- 3개 기능 포함

✅ 좋은 PR:
- 3-5개 파일 변경
- 200-300줄
- 1개 기능
```

**3. Self Review 먼저**

```
PR 올리기 전 스스로 체크:
□ 불필요한 로그는 제거했는가?
□ 주석처리된 코드는 삭제했는가?
□ import는 정리했는가?
□ 테스트는 통과하는가?
□ 컨벤션을 따랐는가?
```

**4. 피드백 수용**

```
✅ "좋은 지적 감사합니다. 수정하겠습니다."
✅ "제가 놓친 부분이네요. 개선하겠습니다."
✅ "의견 주셔서 감사합니다. 다만 XX 이유로 현재 방식을 유지하려 합니다."

❌ "이게 왜 문제인가요?"
❌ "원래 이렇게 하는 거예요."
```

### 체크리스트

```
기능성:
□ 요구사항을 충족하는가?
□ 엣지 케이스를 처리하는가?
□ 에러 처리가 적절한가?

코드 품질:
□ 읽기 쉬운가?
□ 중복 코드는 없는가?
□ 네이밍이 명확한가?
□ 함수가 한 가지 일만 하는가?

테스트:
□ 테스트가 작성되었는가?
□ 테스트가 의미있는가?
□ 커버리지가 충분한가?

성능:
□ N+1 쿼리는 없는가?
□ 불필요한 객체 생성은 없는가?
□ 캐싱이 필요한 부분은 없는가?

보안:
□ SQL Injection 가능성은 없는가?
□ XSS 가능성은 없는가?
□ 민감 정보가 로그에 남지 않는가?
```

---

## Git 워크플로우

### 브랜치 전략

```
main (production)
  ↑
develop (staging)
  ↑
feature/order-creation
feature/payment-gateway
feature/user-authentication
```

**브랜치 네이밍:**

```
✅ 좋은 예:
feature/user-authentication
bugfix/null-pointer-in-order-service
hotfix/payment-gateway-timeout
refactor/order-service-cleanup

❌ 나쁜 예:
fix
temp
test
my-branch
```

### 커밋 메시지

```
❌ 나쁜 커밋:
"수정"
"bugfix"
"asdf"
"WIP"

✅ 좋은 커밋:
"feat: 주문 생성 API 추가"
"fix: 재고 차감 시 동시성 이슈 해결"
"refactor: OrderService 메서드 분리"
"test: Order 도메인 테스트 추가"
"docs: API 문서 업데이트"
```

**커밋 메시지 규칙:**

```
<type>: <subject>

<body>

<footer>
```

**Type:**
- `feat`: 새 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `test`: 테스트 추가/수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅 (기능 변경 없음)
- `chore`: 빌드, 설정 변경

**예시:**

```
feat: 주문 취소 기능 추가

고객이 주문 후 24시간 이내에 취소할 수 있도록 기능을 추가했습니다.

- Order 도메인에 cancel() 메서드 추가
- 취소 가능 여부 검증 로직 구현
- 취소 시 재고 복구 로직 추가

Closes #123
```

### Pull Request 가이드

**1. 하나의 PR = 하나의 목적**

```
❌ 나쁜 PR:
"사용자 기능 개선"
- 사용자 생성 API 추가
- 비밀번호 변경 기능
- 프로필 이미지 업로드
- 이메일 인증
(4개 기능을 한 PR에)

✅ 좋은 PR:
"feat: 사용자 생성 API 추가"
(1개 기능만)
```

**2. Draft PR 활용**

```
초기 피드백이 필요할 때:
1. Draft PR로 먼저 올리기
2. "설계 방향에 대한 의견 부탁드립니다" 코멘트
3. 피드백 반영 후 Ready for Review
```

**3. 리뷰 반영 후 알림**

```
리뷰어의 코멘트에:
- ✅ 수정 완료 시: "수정 완료했습니다" + Resolve
- ❓ 질문 시: 답변 후 논의
- 📝 다음 PR에서 처리: "다음 PR에서 개선하겠습니다 #<이슈번호>"
```

### Merge 전 체크리스트

```
□ CI/CD 파이프라인 통과
□ 모든 코멘트 해결
□ 최소 1명 이상 Approve
□ 컨플릭트 해결
□ Squash/Rebase 여부 확인
□ develop/main에서 최신 코드 반영
```

---

## 요약

| 주제 | 핵심 원칙 |
|------|----------|
| **읽기 좋은 코드** | 한 가지 일만, Early Return, 매직 넘버 제거 |
| **네이밍** | 명확하고 일관성 있게, 약어 지양 |
| **주석** | Why를 설명, 자기문서화 코드 우선 |
| **코드 리뷰** | 친절하고 구체적으로, 작은 PR |
| **Git** | 명확한 커밋 메시지, 기능별 브랜치 |

**기억하세요:**
- 코드는 팀의 자산입니다
- 6개월 후의 나도 이해할 수 있게 작성하세요
- 완벽한 코드보다 개선하는 문화가 중요합니다

---

**이전**: ← [실용적인 코드 구조](./03-code-structure.md)
**처음으로**: [실무 중심 Java 프로그래밍 가이드](./README.md)
