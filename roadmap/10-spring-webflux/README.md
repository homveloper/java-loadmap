# 10. Spring WebFlux (반응형 프로그래밍)

> **학습 기간**: 1-2주
> **난이도**: ⭐⭐⭐⭐⭐
> **전제 조건**: Spring Web MVC, Java 동시성

## 📚 학습 목표

C++ Boost ASIO 소켓 서버 경험을 활용하여 고성능 비동기 서버를 구축합니다.

## 🎯 WebFlux vs WebMVC

| 특성 | WebMVC | WebFlux |
|-----|--------|---------|
| **모델** | 동기/블로킹 | 비동기/논블로킹 |
| **스레드** | Thread-per-request | 이벤트 루프 (Netty) |
| **처리량** | 보통 | 높음 (높은 동시성) |
| **학습 곡선** | 낮음 | 높음 |
| **DB 지원** | JDBC | R2DBC (Reactive) |

## 🎯 핵심 내용

### 1. Mono와 Flux

```java
// Mono: 0 또는 1개의 값
Mono<String> mono = Mono.just("Hello");
Mono<String> empty = Mono.empty();

// Flux: 0-N개의 값 (스트림)
Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5);
Flux<String> range = Flux.range(1, 10)
    .map(i -> "Number " + i);

// 구독 (subscribe)
mono.subscribe(value -> System.out.println(value));

numbers.subscribe(
    value -> System.out.println("Value: " + value),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);
```

### 2. WebFlux Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<User> getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public Mono<User> createUser(@RequestBody Mono<User> userMono) {
        return userMono.flatMap(userService::save);
    }

    // Server-Sent Events (실시간 스트리밍)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamUsers() {
        return userService.findAll()
            .delayElements(Duration.ofSeconds(1));
    }
}
```

### 3. Reactive Repository (R2DBC)

```java
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Flux<User> findByUsername(String username);

    @Query("SELECT * FROM users WHERE email = :email")
    Mono<User> findByEmail(String email);
}

// Service
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    public Mono<User> save(User user) {
        return userRepository.save(user);
    }
}
```

### 4. 연산자 체이닝

```java
public Mono<UserResponse> processUser(Long userId) {
    return userRepository.findById(userId)
        // map: 변환
        .map(user -> new UserResponse(user.getId(), user.getName()))
        // flatMap: 비동기 변환
        .flatMap(response -> enrichWithOrders(response))
        // filter: 필터링
        .filter(response -> response.getOrderCount() > 0)
        // defaultIfEmpty: 기본값
        .defaultIfEmpty(UserResponse.empty())
        // doOnNext: 사이드 이펙트
        .doOnNext(response -> log.info("User: {}", response))
        // onErrorResume: 에러 처리
        .onErrorResume(error -> {
            log.error("Error processing user", error);
            return Mono.just(UserResponse.error());
        });
}

private Mono<UserResponse> enrichWithOrders(UserResponse response) {
    return orderRepository.countByUserId(response.getId())
        .map(count -> {
            response.setOrderCount(count);
            return response;
        });
}
```

### 5. 병렬 처리

```java
public Mono<CombinedResponse> getCombinedData(Long userId) {
    Mono<User> userMono = userRepository.findById(userId);
    Mono<List<Order>> ordersMono = orderRepository.findByUserId(userId).collectList();
    Mono<Statistics> statsMono = statisticsService.getStatistics(userId);

    // 3개 요청을 병렬로 실행
    return Mono.zip(userMono, ordersMono, statsMono)
        .map(tuple -> new CombinedResponse(
            tuple.getT1(),
            tuple.getT2(),
            tuple.getT3()
        ));
}
```

### 6. WebClient (비동기 HTTP 클라이언트)

```java
@Service
public class ExternalApiClient {

    private final WebClient webClient;

    public ExternalApiClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.example.com").build();
    }

    public Mono<UserDto> getUser(Long id) {
        return webClient.get()
            .uri("/users/{id}", id)
            .retrieve()
            .bodyToMono(UserDto.class);
    }

    public Flux<Post> getPosts(Long userId) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/posts")
                .queryParam("userId", userId)
                .build())
            .retrieve()
            .bodyToFlux(Post.class);
    }

    public Mono<User> createUser(User user) {
        return webClient.post()
            .uri("/users")
            .bodyValue(user)
            .retrieve()
            .bodyToMono(User.class);
    }
}
```

### 7. BackPressure

```java
// 생산 속도 > 소비 속도 문제 해결
Flux.range(1, 1000)
    .onBackpressureBuffer(100)  // 버퍼링
    .subscribe(new BaseSubscriber<Integer>() {
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            request(10);  // 처음 10개만 요청
        }

        @Override
        protected void hookOnNext(Integer value) {
            process(value);
            request(1);  // 하나 처리 후 다음 요청
        }
    });
```

## 🎮 게임 서버 활용 예시

### 실시간 플레이어 위치 브로드캐스팅

```java
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final Sinks.Many<PlayerPosition> positionSink =
        Sinks.many().multicast().onBackpressureBuffer();

    // 플레이어 위치 업데이트
    @PostMapping("/position")
    public Mono<Void> updatePosition(@RequestBody PlayerPosition position) {
        positionSink.tryEmitNext(position);
        return Mono.empty();
    }

    // 실시간 위치 스트리밍 (SSE)
    @GetMapping(value = "/positions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PlayerPosition> streamPositions() {
        return positionSink.asFlux();
    }

    // 특정 플레이어만 필터링
    @GetMapping(value = "/positions/{playerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PlayerPosition> streamPlayerPosition(@PathVariable String playerId) {
        return positionSink.asFlux()
            .filter(pos -> pos.getPlayerId().equals(playerId));
    }
}
```

## 📖 학습 리소스

- [Project Reactor 공식 문서](https://projectreactor.io/docs/core/release/reference/)
- [Spring WebFlux 공식 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)

## ✅ 체크리스트

- [ ] Mono와 Flux 이해
- [ ] WebFlux Controller 작성
- [ ] R2DBC Repository
- [ ] map, flatMap, filter 연산자
- [ ] WebClient 활용
- [ ] BackPressure 이해
- [ ] 실시간 스트리밍 (SSE)

## 🚀 다음 단계

**→ [11. 메시징 & 이벤트 기반 아키텍처](../11-messaging-events/)**

## 💡 C++ ASIO 개발자를 위한 팁

- **이벤트 루프**: Netty의 이벤트 루프 = ASIO의 io_context
- **비동기 I/O**: Reactor 패턴 공통
- **콜백 vs Reactive**: 콜백 지옥 대신 체이닝
