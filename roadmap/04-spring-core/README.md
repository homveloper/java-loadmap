# 04. Spring Core & 의존성 주입

> **학습 기간**: 1주
> **난이도**: ⭐⭐⭐☆☆
> **전제 조건**: Java 기초, OOP 이해

## 📚 학습 목표

Spring의 핵심 원리인 IoC (Inversion of Control)와 DI (Dependency Injection)를 이해합니다.

## 🎯 핵심 내용

### 1. IoC (Inversion of Control)
- 제어의 역전 개념
- 프레임워크가 객체 생명주기 관리
- ApplicationContext

### 2. DI (Dependency Injection)
```java
// 생성자 주입 (권장)
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired  // 생성자가 하나면 생략 가능
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// 필드 주입 (비권장)
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### 3. Bean 스코프
- **Singleton**: 기본, 애플리케이션당 하나
- **Prototype**: 요청마다 새 인스턴스
- **Request**: HTTP 요청당 하나 (Web)
- **Session**: HTTP 세션당 하나 (Web)

### 4. Bean 생명주기
```java
@Component
public class MyBean {
    @PostConstruct
    public void init() {
        // 초기화 로직
    }

    @PreDestroy
    public void cleanup() {
        // 정리 로직
    }
}
```

### 5. AOP (Aspect Oriented Programming)
```java
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Method: " + joinPoint.getSignature().getName());
    }
}
```

## 📖 학습 리소스

- [Spring Framework 공식 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html)
- 토비의 스프링 3.1
- [백기선 - 스프링 프레임워크 핵심 기술](https://www.inflearn.com/course/spring-framework_core)

## ✅ 체크리스트

- [ ] IoC와 DI 개념 이해
- [ ] @Component, @Service, @Repository
- [ ] 생성자 주입 vs 필드 주입
- [ ] Bean 스코프 차이
- [ ] AOP 기본 개념

## 🚀 다음 단계

**→ [05. Spring Boot 기초](../05-spring-boot/)**
