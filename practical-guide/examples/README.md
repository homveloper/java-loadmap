# 실전 예제 코드

이 디렉토리는 실무 중심 Java 프로그래밍 가이드의 예제 코드를 포함합니다.

## 📁 파일 구조

### 핵심 패턴
- `Result.java` - Error as Value 패턴 구현
- `Error.java` - 에러 정보 담는 불변 객체

### Before/After 리팩토링
- `BeforeRefactoring.java` - 안티패턴 예시
- `AfterRefactoring.java` - 리팩토링 후 모범 사례

## 🎯 Result 패턴 사용법

### 기본 사용

```java
// 성공 케이스
Result<User> result = Result.success(user);

// 실패 케이스
Result<User> result = Result.failure(
    Error.validation("유효하지 않은 이메일")
);
```

### 함수형 체이닝

```java
Result<OrderDto> result = createOrder(customerId, items)
    .map(order -> new OrderDto(order.getId(), order.getTotal()))
    .flatMap(dto -> enrichWithCustomerInfo(dto));

// 사용
if (result.isSuccess()) {
    OrderDto dto = result.getValue();
    return ResponseEntity.ok(dto);
} else {
    Error error = result.getError();
    return ResponseEntity.badRequest().body(error);
}
```

### Pattern Matching (Java 21+)

```java
return switch (result) {
    case Result.Success<Order> s ->
        ResponseEntity.ok(s.value());
    case Result.Failure<Order> f ->
        ResponseEntity.badRequest().body(f.error());
};
```

## 📖 리팩토링 비교

### Before (안티패턴)
- God Class - 모든 로직이 서비스에
- Anemic Domain Model - 비즈니스 로직 없는 도메인
- 의미없는 getter/setter
- 매직 넘버/문자열
- 깊은 중첩

### After (모범 사례)
- Rich Domain Model - 비즈니스 로직이 도메인에
- 불변성 - final 필드, setter 없음
- Enum으로 타입 안전성
- Early Return으로 가독성
- Result 패턴으로 명시적 에러 처리

## 🚀 실행 방법

이 예제들은 컴파일 가능한 실제 Java 코드입니다.

```bash
# 컴파일
javac practical/guide/examples/*.java

# 실행 (각 클래스는 독립적인 예제)
java practical.guide.examples.BeforeRefactoring
java practical.guide.examples.AfterRefactoring
```

## 💡 학습 포인트

1. **Result.java**를 먼저 보세요
   - Go의 `(value, error)` 패턴을 Java로 구현
   - 함수형 메서드 체이닝 제공

2. **BeforeRefactoring.java**에서 안티패턴 확인
   - 실무에서 자주 보는 나쁜 패턴들
   - 왜 나쁜지 주석으로 설명

3. **AfterRefactoring.java**에서 개선된 코드 확인
   - 같은 기능을 더 나은 방식으로 구현
   - 각 개선점에 대한 설명 포함

## 🔗 관련 가이드

- [안티패턴 피하기](../01-anti-patterns.md)
- [현대적 Java 기능](../02-modern-java.md)
- [실용적인 코드 구조](../03-code-structure.md)
