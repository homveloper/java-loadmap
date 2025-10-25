# Java 웹 백엔드 개발자 학습 로드맵

> **대상**: Golang 웹 백엔드 + C++ 소켓 서버 경험자 (게임 서버 도메인)
> **목표**: Java + Spring 웹 백엔드 개발자 이직
> **예상 기간**: 3-4개월 (집중 학습 기준)

## 📋 전체 로드맵 개요

```
Phase 1: Java 기초 다지기 (3-4주)
├─ 01. Java 언어 기초
├─ 02. JVM과 메모리 관리
└─ 03. Java 동시성과 멀티스레딩

Phase 2: Spring 프레임워크 핵심 (4-5주)
├─ 04. Spring Core & 의존성 주입
├─ 05. Spring Boot 기초
├─ 06. Spring Web MVC
├─ 07. Spring Data JPA & Database
└─ 08. Spring Security & 인증/인가

Phase 3: 실전 프로젝트 & 고급 주제 (5-6주)
├─ 09. 실전 프로젝트 1: RESTful API 서버
├─ 10. Spring WebFlux (반응형 프로그래밍)
├─ 11. 메시징 & 이벤트 기반 아키텍처
└─ 12. 실전 프로젝트 2: 마이크로서비스

Phase 4: 프로덕션 준비 (2-3주)
├─ 13. 테스트 전략 (Unit, Integration, E2E)
├─ 14. 성능 최적화 & 모니터링
└─ 15. CI/CD & 배포 전략
```

---

## 🎯 학습 로드맵 상세

### Phase 1: Java 기초 다지기 (3-4주)

#### 01. Java 언어 기초 (1-2주)
**목표**: Golang 경험을 활용하여 Java 문법 및 OOP 개념 습득

**핵심 학습 내용**:
- Java 기본 문법 (변수, 제어문, 메서드)
- 객체지향 프로그래밍 (클래스, 인터페이스, 상속, 다형성)
- 제네릭과 컬렉션 프레임워크
- 람다와 Stream API (Golang의 함수형 접근과 비교)
- 예외 처리 (defer vs try-catch-finally)
- Java 11/17/21 최신 기능

**Golang과의 비교 포인트**:
- 명시적 클래스 vs 구조체
- 인터페이스 구현 방식 차이
- 에러 처리 패턴 차이

📚 **상세 가이드**: [roadmap/01-java-basics/](./roadmap/01-java-basics/)

---

#### 02. JVM과 메모리 관리 (1주)
**목표**: C++ 메모리 관리 경험을 활용하여 JVM 이해

**핵심 학습 내용**:
- JVM 아키텍처 (Class Loader, Runtime Data Area, Execution Engine)
- 가비지 컬렉션 (GC) 알고리즘
- 힙, 스택, 메타스페이스
- JIT 컴파일러
- 메모리 누수 디버깅
- 프로파일링 도구 (VisualVM, JProfiler)

**C++과의 비교 포인트**:
- 수동 메모리 관리 vs GC
- RAII vs try-with-resources
- 스마트 포인터 vs 참조

📚 **상세 가이드**: [roadmap/02-jvm-memory/](./roadmap/02-jvm-memory/)

---

#### 03. Java 동시성과 멀티스레딩 (1-2주)
**목표**: Golang의 goroutine, C++의 스레드 경험을 Java로 전환

**핵심 학습 내용**:
- Thread와 Runnable
- synchronized와 volatile
- java.util.concurrent 패키지
- ExecutorService와 Thread Pool
- CompletableFuture (비동기 프로그래밍)
- 동시성 컬렉션 (ConcurrentHashMap, BlockingQueue)
- Lock, Semaphore, CountDownLatch
- 일반적인 동시성 문제 (Deadlock, Race Condition)

**비교 포인트**:
- Golang goroutine + channel vs Java Thread + BlockingQueue
- C++ mutex/condition_variable vs Java synchronized/Lock
- 경량 스레드 vs OS 스레드

📚 **상세 가이드**: [roadmap/03-java-concurrency/](./roadmap/03-java-concurrency/)

---

### Phase 2: Spring 프레임워크 핵심 (4-5주)

#### 04. Spring Core & 의존성 주입 (1주)
**목표**: Spring의 핵심 원리인 IoC와 DI 이해

**핵심 학습 내용**:
- IoC (Inversion of Control) 개념
- DI (Dependency Injection) 패턴
- Bean과 ApplicationContext
- 어노테이션 기반 설정 (@Component, @Autowired, @Configuration)
- Bean 스코프와 생명주기
- AOP (Aspect Oriented Programming) 기초

**Golang과의 비교**:
- Wire, Dig 등 DI 라이브러리와 비교
- 명시적 의존성 vs 자동 주입

📚 **상세 가이드**: [roadmap/04-spring-core/](./roadmap/04-spring-core/)

---

#### 05. Spring Boot 기초 (1-2주)
**목표**: Spring Boot로 빠른 애플리케이션 개발

**핵심 학습 내용**:
- Spring Boot 자동 설정 (Auto Configuration)
- application.properties / application.yml 설정
- 프로파일 (dev, prod 환경 분리)
- Starter Dependencies
- Actuator (헬스체크, 메트릭)
- 빌드 도구 (Maven, Gradle)

**Golang과의 비교**:
- Viper 등 설정 관리와 비교
- 프로젝트 구조 차이

📚 **상세 가이드**: [roadmap/05-spring-boot/](./roadmap/05-spring-boot/)

---

#### 06. Spring Web MVC (1주)
**목표**: RESTful API 서버 구축

**핵심 학습 내용**:
- @RestController, @RequestMapping
- HTTP 메서드 매핑 (@GetMapping, @PostMapping 등)
- 요청/응답 처리 (@RequestBody, @ResponseBody, @PathVariable)
- DTO와 Validation (@Valid, Bean Validation)
- 예외 처리 (@ExceptionHandler, @ControllerAdvice)
- CORS 설정

**Golang과의 비교**:
- Gin, Echo 프레임워크와 비교
- 라우팅 방식 차이
- 미들웨어 vs Interceptor/Filter

📚 **상세 가이드**: [roadmap/06-spring-web-mvc/](./roadmap/06-spring-web-mvc/)

---

#### 07. Spring Data JPA & Database (1-2주)
**목표**: 데이터베이스 연동 및 ORM 활용

**핵심 학습 내용**:
- JPA (Java Persistence API) 개념
- Entity, Repository 패턴
- CRUD 작업
- Query Methods, @Query
- 연관관계 매핑 (1:N, N:M)
- 트랜잭션 관리 (@Transactional)
- N+1 문제와 해결 (Fetch Join, EntityGraph)
- 데이터베이스 마이그레이션 (Flyway, Liquibase)

**Golang과의 비교**:
- GORM과 비교
- 명시적 쿼리 vs ORM

📚 **상세 가이드**: [roadmap/07-spring-data-jpa/](./roadmap/07-spring-data-jpa/)

---

#### 08. Spring Security & 인증/인가 (1주)
**목표**: 보안 기능 구현

**핵심 학습 내용**:
- Spring Security 아키텍처
- 인증 (Authentication) vs 인가 (Authorization)
- JWT 기반 인증
- SecurityFilterChain
- Password Encoding
- Role 기반 접근 제어 (@PreAuthorize, @Secured)
- CSRF, CORS 보안

**Golang과의 비교**:
- JWT 미들웨어와 비교
- 세션 vs 토큰 기반 인증

📚 **상세 가이드**: [roadmap/08-spring-security/](./roadmap/08-spring-security/)

---

### Phase 3: 실전 프로젝트 & 고급 주제 (5-6주)

#### 09. 실전 프로젝트 1: RESTful API 서버 (2주)
**목표**: 실무 수준의 REST API 서버 구축

**프로젝트 요구사항**:
- 사용자 관리 (회원가입, 로그인, JWT 인증)
- 게시판 CRUD (페이징, 검색, 정렬)
- 파일 업로드/다운로드
- API 문서화 (Swagger/OpenAPI)
- 단위 테스트 & 통합 테스트
- Docker 컨테이너화

**게임 서버 경험 활용**:
- 사용자 세션 관리
- 실시간 데이터 조회 최적화

📚 **상세 가이드**: [roadmap/09-project-rest-api/](./roadmap/09-project-rest-api/)

---

#### 10. Spring WebFlux (반응형 프로그래밍) (1-2주)
**목표**: 고성능 비동기 서버 구축 (게임 서버 경험 활용)

**핵심 학습 내용**:
- Reactive Programming 개념
- Project Reactor (Mono, Flux)
- WebFlux vs WebMVC
- 논블로킹 I/O
- Reactive Repository
- BackPressure 처리

**C++ ASIO와의 비교**:
- 비동기 I/O 모델 비교
- 이벤트 루프 vs Reactive Streams
- 높은 동시성 처리 전략

📚 **상세 가이드**: [roadmap/10-spring-webflux/](./roadmap/10-spring-webflux/)

---

#### 11. 메시징 & 이벤트 기반 아키텍처 (1-2주)
**목표**: 비동기 메시징 시스템 구축

**핵심 학습 내용**:
- 메시지 큐 개념 (Kafka, RabbitMQ)
- Spring Kafka
- Producer/Consumer 패턴
- 이벤트 기반 아키텍처
- Pub/Sub 패턴
- 메시지 신뢰성 보장
- Dead Letter Queue

**게임 서버 경험 활용**:
- 이벤트 기반 통신
- 메시지 브로드캐스팅

📚 **상세 가이드**: [roadmap/11-messaging-events/](./roadmap/11-messaging-events/)

---

#### 12. 실전 프로젝트 2: 마이크로서비스 (3-4주)
**목표**: 마이크로서비스 아키텍처 설계 및 구현

**프로젝트 요구사항**:
- 최소 2-3개의 독립 서비스
- API Gateway (Spring Cloud Gateway)
- 서비스 간 통신 (REST, gRPC, 메시징)
- 분산 트랜잭션 (Saga 패턴)
- 서비스 디스커버리 (Eureka, Consul)
- 분산 추적 (Zipkin, Jaeger)
- 중앙 집중식 설정 관리
- Docker Compose / Kubernetes 배포

**아키텍처 예시**:
- User Service (인증/인가)
- Game Service (게임 로직)
- Leaderboard Service (랭킹)
- Notification Service (알림)

📚 **상세 가이드**: [roadmap/12-project-microservices/](./roadmap/12-project-microservices/)

---

### Phase 4: 프로덕션 준비 (2-3주)

#### 13. 테스트 전략 (1주)
**목표**: 종합적인 테스트 전략 수립

**핵심 학습 내용**:
- JUnit 5 심화
- Mockito를 이용한 Mock 테스트
- @SpringBootTest, @WebMvcTest, @DataJpaTest
- Testcontainers (Docker 기반 통합 테스트)
- API 테스트 (RestAssured)
- 테스트 커버리지 (JaCoCo)
- TDD/BDD 방법론

📚 **상세 가이드**: [roadmap/13-testing-strategy/](./roadmap/13-testing-strategy/)

---

#### 14. 성능 최적화 & 모니터링 (1-2주)
**목표**: 프로덕션 레벨의 성능 달성

**핵심 학습 내용**:
- JVM 튜닝 (힙 크기, GC 옵션)
- 데이터베이스 쿼리 최적화
- 캐싱 전략 (Redis, Caffeine)
- Connection Pool 튜닝
- 로깅 전략 (Logback, SLF4J)
- 메트릭 수집 (Micrometer, Prometheus)
- APM 도구 (New Relic, Datadog)
- 프로파일링 및 병목 지점 분석

**게임 서버 경험 활용**:
- 낮은 레이턴시 요구사항
- 높은 처리량 최적화
- 실시간 모니터링

📚 **상세 가이드**: [roadmap/14-performance-monitoring/](./roadmap/14-performance-monitoring/)

---

#### 15. CI/CD & 배포 전략 (1주)
**목표**: 자동화된 배포 파이프라인 구축

**핵심 학습 내용**:
- GitHub Actions / Jenkins
- 빌드 자동화
- 테스트 자동화
- Docker 이미지 빌드 및 푸시
- Kubernetes 배포 (Deployment, Service, Ingress)
- Helm Charts
- 무중단 배포 (Blue-Green, Canary)
- 환경별 설정 관리

📚 **상세 가이드**: [roadmap/15-cicd-deployment/](./roadmap/15-cicd-deployment/)

---

## 🚀 학습 전략 및 팁

### 1. 기존 경험 활용하기

| 기존 기술 | Java/Spring 대응 | 활용 전략 |
|---------|-----------------|---------|
| Golang 웹 서버 (Gin/Echo) | Spring Web MVC | 라우팅, 미들웨어 개념을 컨트롤러, 인터셉터로 전환 |
| Golang goroutine + channel | Java Thread + ExecutorService | 동시성 패턴 비교 학습 |
| C++ Boost ASIO | Spring WebFlux | 비동기 I/O 모델 비교 |
| C++ 메모리 관리 | JVM GC | 메모리 프로파일링 경험 활용 |
| 게임 서버 (실시간 통신) | WebSocket, Server-Sent Events | 실시간 기능 구현 |
| 게임 서버 (높은 처리량) | 성능 최적화, 캐싱 | 병목 지점 분석 및 튜닝 |

### 2. 단계별 학습 방법

**1단계: 개념 학습 (20%)**
- 공식 문서 및 튜토리얼
- 관련 서적 읽기
- 온라인 강의 수강

**2단계: 실습 (60%)**
- 작은 예제 프로젝트 구현
- 기존 Golang 프로젝트를 Java로 포팅
- 코드 작성 및 디버깅

**3단계: 정리 및 복습 (20%)**
- 블로그 작성 또는 문서화
- GitHub 커밋 및 README 작성
- 핵심 개념 정리

### 3. 주간 학습 계획 예시

**주중 (월-금)**:
- 평일 2-3시간 집중 학습
- 개념 학습 및 코드 실습
- 작은 단위 구현 및 테스트

**주말 (토-일)**:
- 4-6시간 프로젝트 작업
- 주간 학습 내용 통합
- 실전 프로젝트 진행

### 4. 추천 학습 리소스

**온라인 강의**:
- [백기선의 스프링 부트](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8) (한글)
- [김영한의 스프링 완전 정복 로드맵](https://www.inflearn.com/roadmaps/373) (한글)
- Spring.io 공식 가이드

**서적**:
- Effective Java (Joshua Bloch) - Java 모범 사례
- Java Concurrency in Practice (Brian Goetz) - 동시성
- 토비의 스프링 3.1 - Spring 핵심 원리
- 스프링 부트와 AWS로 혼자 구현하는 웹 서비스

**커뮤니티**:
- Stack Overflow
- 백엔드 개발자 커뮤니티
- Spring 한국 사용자 그룹

### 5. 포트폴리오 구성

**필수 프로젝트**:
1. RESTful API 서버 (CRUD + 인증)
2. 실시간 기능이 포함된 서비스 (WebSocket/WebFlux)
3. 마이크로서비스 아키텍처 프로젝트

**GitHub 저장소 체크리스트**:
- [ ] 명확한 README (프로젝트 설명, 기술 스택, 실행 방법)
- [ ] 아키텍처 다이어그램
- [ ] API 문서 (Swagger/Postman Collection)
- [ ] 단위/통합 테스트
- [ ] Docker / Docker Compose 설정
- [ ] CI/CD 파이프라인
- [ ] 배포 가이드

---

## 📝 면접 준비

### Java 기술 면접 주요 질문

**Java 기초**:
- Java의 특징 (OOP, Platform Independent)
- JVM, JRE, JDK 차이
- 접근 제어자 (public, private, protected, default)
- equals()와 hashCode()
- String, StringBuilder, StringBuffer 차이
- final, finally, finalize
- 추상 클래스 vs 인터페이스

**Java 고급**:
- 제네릭과 타입 소거
- 람다와 Stream API
- Optional 사용법
- CompletableFuture
- 가비지 컬렉션 알고리즘
- 메모리 누수 원인 및 해결

**Spring**:
- IoC와 DI 개념
- Bean 생명주기
- AOP 개념 및 활용
- @Transactional 동작 원리
- JPA N+1 문제
- Spring Security 인증/인가 흐름

**시스템 설계**:
- 마이크로서비스 아키텍처 장단점
- API Gateway 역할
- 서비스 간 통신 방법
- 분산 트랜잭션 처리
- 캐싱 전략
- 성능 최적화 경험

**경험 기반 질문**:
- Golang에서 Java로 전환한 이유
- 게임 서버 경험을 웹 서버에 어떻게 적용했는지
- 성능 병목을 해결한 경험
- 어려웠던 기술적 문제와 해결 과정

---

## 🎯 3개월 집중 학습 계획

### 1개월차: Java 기초 + Spring 핵심
- 주 1-2주: Java 언어 기초
- 주 3주: JVM & 동시성
- 주 4주: Spring Core & Boot 기초

### 2개월차: Spring 심화 + 첫 프로젝트
- 주 1-2주: Spring Web MVC + Data JPA
- 주 3주: Spring Security
- 주 4주: 실전 프로젝트 1 (REST API)

### 3개월차: 고급 주제 + 마이크로서비스
- 주 1-2주: WebFlux + 메시징
- 주 3-4주: 실전 프로젝트 2 (마이크로서비스)

### 4개월차: 프로덕션 준비 + 면접 준비
- 주 1-2주: 테스트 + 성능 최적화
- 주 3주: CI/CD + 배포
- 주 4주: 포트폴리오 정리 + 면접 준비

---

## ✅ 체크리스트

### 학습 진행 체크리스트
- [ ] Phase 1: Java 기초 다지기
  - [ ] 01. Java 언어 기초
  - [ ] 02. JVM과 메모리 관리
  - [ ] 03. Java 동시성과 멀티스레딩
- [ ] Phase 2: Spring 프레임워크 핵심
  - [ ] 04. Spring Core & 의존성 주입
  - [ ] 05. Spring Boot 기초
  - [ ] 06. Spring Web MVC
  - [ ] 07. Spring Data JPA & Database
  - [ ] 08. Spring Security & 인증/인가
- [ ] Phase 3: 실전 프로젝트 & 고급 주제
  - [ ] 09. 실전 프로젝트 1: RESTful API 서버
  - [ ] 10. Spring WebFlux
  - [ ] 11. 메시징 & 이벤트 기반 아키텍처
  - [ ] 12. 실전 프로젝트 2: 마이크로서비스
- [ ] Phase 4: 프로덕션 준비
  - [ ] 13. 테스트 전략
  - [ ] 14. 성능 최적화 & 모니터링
  - [ ] 15. CI/CD & 배포 전략

### 포트폴리오 체크리스트
- [ ] GitHub 프로필 정리
- [ ] 3개 이상의 완성된 프로젝트
- [ ] 각 프로젝트 README 작성
- [ ] 기술 블로그 또는 문서 정리
- [ ] LinkedIn 프로필 업데이트

### 이직 준비 체크리스트
- [ ] 이력서 작성 (Java 기술 스택 강조)
- [ ] 포트폴리오 정리
- [ ] 기술 면접 질문 정리
- [ ] 코딩 테스트 준비 (LeetCode, 프로그래머스)
- [ ] 행동 면접 답변 준비

---

## 🔗 다음 단계

각 단계별 상세 로드맵은 `roadmap/` 디렉토리에서 확인하세요:

1. [Java 언어 기초](./roadmap/01-java-basics/)
2. [JVM과 메모리 관리](./roadmap/02-jvm-memory/)
3. [Java 동시성과 멀티스레딩](./roadmap/03-java-concurrency/)
4. [Spring Core & 의존성 주입](./roadmap/04-spring-core/)
5. [Spring Boot 기초](./roadmap/05-spring-boot/)
6. [Spring Web MVC](./roadmap/06-spring-web-mvc/)
7. [Spring Data JPA & Database](./roadmap/07-spring-data-jpa/)
8. [Spring Security & 인증/인가](./roadmap/08-spring-security/)
9. [실전 프로젝트 1: RESTful API 서버](./roadmap/09-project-rest-api/)
10. [Spring WebFlux (반응형 프로그래밍)](./roadmap/10-spring-webflux/)
11. [메시징 & 이벤트 기반 아키텍처](./roadmap/11-messaging-events/)
12. [실전 프로젝트 2: 마이크로서비스](./roadmap/12-project-microservices/)
13. [테스트 전략](./roadmap/13-testing-strategy/)
14. [성능 최적화 & 모니터링](./roadmap/14-performance-monitoring/)
15. [CI/CD & 배포 전략](./roadmap/15-cicd-deployment/)

---

**Good luck with your Java journey! 🚀**
