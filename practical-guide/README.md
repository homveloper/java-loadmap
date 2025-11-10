ㄱ# 💎 실무 중심 Java 프로그래밍 가이드

> **대상**: 3-5년차 중급 개발자
> **목표**: 이론보다 실용, 과도한 OOP 지양, 현대적 Java 활용

## 🎯 이 가이드의 철학

**교과서가 가르치는 것 vs 실무에서 필요한 것**

- ❌ "모든 것을 인터페이스로 추상화하세요"
- ✅ "필요할 때만 추상화하세요"

- ❌ "private 필드 + getter/setter가 캡슐화입니다"
- ✅ "불변성과 도메인 메서드로 진짜 캡슐화를 하세요"

- ❌ "디자인 패턴을 많이 적용할수록 좋습니다"
- ✅ "문제를 해결하는 가장 단순한 방법을 찾으세요"

**Go 개발자가 느끼는 Java의 문제점**

```go
// Go: 간단하고 직관적
type User struct {
    Name string
    Age  int
}

user := User{Name: "John", Age: 30}
```

```java
// Java: 과도한 의식(ceremony)
public class User {
    private String name;
    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

**하지만 현대적 Java는 다릅니다:**

```java
// Java 14+ Record: Go만큼 간단
public record User(String name, int age) {}

User user = new User("John", 30);
```

## 📚 목차

### [1. 안티패턴 피하기](./01-anti-patterns.md)
실무에서 자주 보는 잘못된 패턴과 해결책

- 과도한 OOP의 폐해
- getter/setter 안티패턴
- 불필요한 인터페이스와 추상화
- God Class와 Anemic Domain Model
- 성능 안티패턴

### [2. 현대적 Java 기능 활용](./02-modern-java.md)
Java 8-21의 실용적 기능들

- Record로 보일러플레이트 제거
- Optional 올바른 사용법
- Stream API 실전 패턴
- 정적 팩토리 메서드
- Result<T> 패턴 (Error as Value)
- Pattern Matching과 Sealed Classes

### [3. 실용적인 코드 구조](./03-code-structure.md)
확장 가능하고 유지보수 쉬운 구조

- 레이어 분리 전략
- 도메인 모델 vs Entity vs DTO
- 의존성 관리
- 패키지 구조
- 언제 추상화할 것인가

### [4. 협업 Best Practices](./04-collaboration.md)
팀에서 함께 일하기 위한 코딩 습관

- 읽기 좋은 코드 작성
- 네이밍 컨벤션
- 주석 vs 자기문서화 코드
- 코드 리뷰 가이드
- Git 워크플로우

## 🎓 학습 방법

### 1. 순서대로 읽을 필요 없습니다
관심있는 주제부터 읽으세요. 각 문서는 독립적입니다.

### 2. Before/After 코드를 비교하세요
이론보다 실제 코드 예시에 집중했습니다.

### 3. 당장 적용할 수 있는 것부터
작은 것부터 시작하세요. 완벽함보다 개선이 중요합니다.

### 4. 팀과 함께 논의하세요
이 가이드의 내용이 절대적 정답은 아닙니다. 팀의 상황에 맞게 조정하세요.

## 🚀 빠른 시작

### 이미 문제를 겪고 있다면

| 문제 | 참고 문서 |
|------|-----------|
| 코드가 너무 복잡해요 | [안티패턴 - 과도한 OOP](./01-anti-patterns.md#과도한-oop) |
| 보일러플레이트가 많아요 | [현대적 Java - Record](./02-modern-java.md#record) |
| null 체크가 지저분해요 | [현대적 Java - Optional](./02-modern-java.md#optional) |
| 레이어 분리가 애매해요 | [코드 구조 - 레이어 분리](./03-code-structure.md#레이어-분리) |
| 코드 리뷰가 힘들어요 | [협업 - 읽기 좋은 코드](./04-collaboration.md#읽기-좋은-코드) |

### Go 개발자라면

Java와 Go의 차이점을 중심으로 설명했습니다:
- [안티패턴 - 불필요한 추상화](./01-anti-patterns.md#불필요한-추상화)
- [현대적 Java - 간결한 코드](./02-modern-java.md)
- [코드 구조 - 단순함 유지하기](./03-code-structure.md#단순함-유지)

## 💡 핵심 원칙

### 1. 단순함이 최고다 (Simplicity)
복잡한 디자인 패턴보다 읽기 쉬운 코드가 낫습니다.

### 2. 필요할 때만 추상화하라 (YAGNI)
미래를 위한 과도한 설계는 독이 됩니다.

### 3. 명시적이 암묵적보다 낫다 (Explicit over Implicit)
Go의 철학을 Java에도 적용할 수 있습니다.

### 4. 실용성이 순수함을 이긴다 (Pragmatic over Pure)
완벽한 OOP보다 동작하는 코드가 중요합니다.

## 🔗 관련 문서

- [Best Practices](../best-practices/): 아키텍처와 설계 철학
- [Blog API Example](../examples/blog-api/): 실제 프로젝트 예시
- [Spring Boot Roadmap](../roadmap/05-spring-boot/): Spring 학습 로드맵

## 📖 참고 자료

### 책
- **Effective Java 3rd Edition** (Joshua Bloch) - Java의 바이블
- **Clean Code** (Robert C. Martin) - 읽기 좋은 코드
- **Refactoring** (Martin Fowler) - 리팩토링 기법

### 온라인
- [JEP (JDK Enhancement Proposals)](https://openjdk.org/jeps/0) - 최신 Java 기능
- [Baeldung](https://www.baeldung.com/) - 실용적인 Java 튜토리얼
- [Spring Blog](https://spring.io/blog) - Spring 공식 블로그

---

## 💬 피드백

이 가이드는 실무 경험을 바탕으로 작성되었지만, 여러분의 의견이 더 좋은 가이드를 만듭니다.

- 잘못된 내용이 있다면 이슈를 열어주세요
- 더 나은 예시가 있다면 PR을 보내주세요
- 궁금한 점이 있다면 Discussion에서 질문해주세요

**Remember**: 이 가이드의 목표는 "올바른" 코드가 아니라 "실용적인" 코드입니다. 🚀
