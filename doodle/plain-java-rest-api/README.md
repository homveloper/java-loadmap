# Plain Java REST API Server

## 주제

**Spring 없이 순수 Java만으로 REST API 서버 구축**

## 학습 목표

- Java 내장 `HttpServer` 이해 및 활용
- Spring 없이 HTTP 요청/응답 처리하는 방법 학습
- 수동 JSON 직렬화/역직렬화 구현
- RESTful API 설계 원칙 이해
- 라우팅과 핸들러 패턴 구현

## 기술 스택

- **Java 17+** (HttpServer 사용)
- **No Framework** - 순수 JDK만 사용
- **No External Dependencies** - 외부 라이브러리 없음

## 프로젝트 구조

```
plain-java-rest-api/
├── README.md
└── src/
    └── main/
        └── java/
            └── com/example/
                ├── RestApiServer.java         # 메인 서버
                ├── model/
                │   ├── User.java              # User 모델
                │   ├── Post.java              # Post 모델
                │   └── Product.java           # Product 모델
                ├── handler/                   # 클래스 기반 핸들러
                │   ├── UserHandler.java       # User API (HttpHandler 상속)
                │   └── PostHandler.java       # Post API (HttpHandler 상속)
                ├── controller/                # 함수형 핸들러
                │   └── ProductController.java # Product API (정적 메서드)
                ├── middleware/                # 미들웨어
                │   ├── Middleware.java        # 인터페이스
                │   ├── MiddlewareChain.java   # 체인 빌더
                │   ├── LoggingMiddleware.java # 로깅
                │   ├── CorsMiddleware.java    # CORS
                │   └── ErrorHandlingMiddleware.java # 에러 처리
                └── util/
                    ├── JsonUtil.java          # JSON 직렬화/역직렬화
                    └── Response.java          # HTTP 응답 헬퍼
```

## 미들웨어 시스템 ⭐

이 프로젝트는 **Golang의 미들웨어 패턴**을 Java로 구현했습니다.

### 구현된 미들웨어

#### 1. LoggingMiddleware - 요청/응답 로깅

모든 HTTP 요청과 응답을 로깅합니다.

**로그 형식:**
```
[2025-11-04 11:30:45] --> GET /api/products
[2025-11-04 11:30:45] <-- GET /api/products 200 (15ms)
```

#### 2. CorsMiddleware - CORS 처리

Cross-Origin Resource Sharing 헤더를 자동으로 추가합니다.

**기능:**
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS`
- OPTIONS preflight 자동 처리

#### 3. ErrorHandlingMiddleware - 통합 에러 처리

모든 예외를 포착하여 일관된 JSON 에러 응답을 반환합니다.

**에러 응답 형식:**
```json
{
  "error": "Resource not found",
  "status": 404,
  "path": "/api/users/999",
  "timestamp": "2025-11-04T11:30:45"
}
```

### 미들웨어 사용법

```java
// 미들웨어 체인 생성
MiddlewareChain middlewares = new MiddlewareChain()
    .use(new LoggingMiddleware())
    .use(new CorsMiddleware())
    .use(new ErrorHandlingMiddleware());

// 핸들러에 미들웨어 적용
server.createContext("/api/users", middlewares.wrap(new UserHandler()));
server.createContext("/api/products", middlewares.wrap(ProductController::handle));
```

### 커스텀 미들웨어 만들기

```java
public class CustomMiddleware implements Middleware {
    @Override
    public void handle(HttpExchange exchange, Runnable next) throws IOException {
        // 전처리
        System.out.println("Before request");

        // 다음 미들웨어/핸들러 실행
        next.run();

        // 후처리
        System.out.println("After request");
    }
}

// 사용
MiddlewareChain chain = new MiddlewareChain()
    .use(new CustomMiddleware())
    .use(new LoggingMiddleware());
```

### Golang 미들웨어와 비교

**Golang (Gin/Echo):**
```go
router.Use(LoggerMiddleware())
router.Use(CORSMiddleware())
router.GET("/api/users", handler)
```

**Java (이 프로젝트):**
```java
MiddlewareChain chain = new MiddlewareChain()
    .use(new LoggingMiddleware())
    .use(new CorsMiddleware());
server.createContext("/api/users", chain.wrap(handler));
```

### 미들웨어 실행 순서

```
요청 → LoggingMiddleware → CorsMiddleware → ErrorHandlingMiddleware → Handler
                                                                          ↓
응답 ← LoggingMiddleware ← CorsMiddleware ← ErrorHandlingMiddleware ← Handler
```

**중요:** 미들웨어는 등록한 순서대로 실행됩니다!
```

## 두 가지 핸들러 방식 비교

이 프로젝트는 **두 가지 핸들러 패턴**을 비교할 수 있도록 구현되어 있습니다.

### 방식 1: 클래스 기반 (UserHandler, PostHandler)

**HttpHandler 인터페이스를 상속받는 전통적인 방식**

```java
// UserHandler.java
public class UserHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // 요청 처리
    }
}

// RestApiServer.java
server.createContext("/api/users", new UserHandler());
```

**특징:**
- ✅ 명확한 클래스 구조
- ✅ 인스턴스 변수 사용 가능
- ✅ 전통적인 OOP 패턴
- ❌ 보일러플레이트 코드 (클래스 선언, implements, @Override)
- ❌ 각 API마다 새로운 클래스 파일 필요

### 방식 2: 함수형 (ProductController)

**정적 메서드와 메서드 참조를 활용한 함수형 방식**

```java
// ProductController.java
public class ProductController {
    // HttpHandler 상속 없음!
    public static void handle(HttpExchange exchange) throws IOException {
        // 요청 처리
    }
}

// RestApiServer.java
server.createContext("/api/products", ProductController::handle);
```

**특징:**
- ✅ 간결한 코드 (클래스 상속 불필요)
- ✅ 메서드 참조로 직접 등록 가능
- ✅ 람다 표현식으로도 사용 가능
- ✅ 정적 메서드로 테스트 쉬움
- ❌ 인스턴스 상태 관리 불가 (정적 변수 사용 필요)

### 람다로 직접 등록하기

더 간단하게 람다 표현식으로 직접 등록할 수도 있습니다:

```java
server.createContext("/api/hello", exchange -> {
    String response = "{\"message\":\"Hello World!\"}";
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    byte[] bytes = response.getBytes("UTF-8");
    exchange.sendResponseHeaders(200, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.getResponseBody().close();
});
```

### 언제 어떤 방식을 사용할까?

| 상황 | 추천 방식 |
|------|-----------|
| 복잡한 상태 관리 필요 | 클래스 기반 |
| 간단한 CRUD API | 함수형 |
| 인스턴스 변수 필요 | 클래스 기반 |
| 빠른 프로토타이핑 | 함수형 (람다) |
| 대규모 프로젝트 | 클래스 기반 |
| 작은 마이크로서비스 | 함수형 |
```

## 구현된 API 엔드포인트

### User API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | 모든 사용자 조회 |
| GET | `/api/users/{id}` | 특정 사용자 조회 |
| POST | `/api/users` | 새 사용자 생성 |
| PUT | `/api/users/{id}` | 사용자 정보 수정 |
| DELETE | `/api/users/{id}` | 사용자 삭제 |

### Post API (클래스 핸들러)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/posts` | 모든 게시글 조회 |
| GET | `/api/posts/{id}` | 특정 게시글 조회 |
| POST | `/api/posts` | 새 게시글 생성 |

### Product API (함수형 핸들러) ⭐

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | 모든 상품 조회 |
| GET | `/api/products/{id}` | 특정 상품 조회 |
| POST | `/api/products` | 새 상품 생성 |
| PUT | `/api/products/{id}` | 상품 정보 수정 |
| DELETE | `/api/products/{id}` | 상품 삭제 |

## 실행 방법

### 1. 컴파일

```bash
cd doodle/plain-java-rest-api

# 모든 Java 파일 컴파일
find src/main/java -name "*.java" -type f | xargs javac -d out

# 또는 명시적으로
javac -d out src/main/java/com/example/*.java \
    src/main/java/com/example/model/*.java \
    src/main/java/com/example/handler/*.java \
    src/main/java/com/example/controller/*.java \
    src/main/java/com/example/middleware/*.java \
    src/main/java/com/example/util/*.java
```

### 2. 실행

```bash
# 컴파일된 클래스 실행
java -cp out com.example.RestApiServer
```

또는 Java 11+ 단일 파일 실행:

```bash
# 모든 파일이 단일 파일에 있다면
java src/main/java/com/example/RestApiServer.java
```

### 3. 서버 시작 확인

```
Server started on http://localhost:8080
```

## API 테스트

### User API 테스트

```bash
# 1. 모든 사용자 조회
curl http://localhost:8080/api/users

# 응답:
# [{"id":1,"name":"홍길동","email":"hong@example.com"},
#  {"id":2,"name":"김철수","email":"kim@example.com"}]

# 2. 특정 사용자 조회
curl http://localhost:8080/api/users/1

# 응답:
# {"id":1,"name":"홍길동","email":"hong@example.com"}

# 3. 새 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"박영희","email":"park@example.com"}'

# 응답:
# {"id":3,"name":"박영희","email":"park@example.com"}

# 4. 사용자 정보 수정
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동(수정)","email":"hong.updated@example.com"}'

# 5. 사용자 삭제
curl -X DELETE http://localhost:8080/api/users/1
```

### Post API 테스트

```bash
# 1. 모든 게시글 조회
curl http://localhost:8080/api/posts

# 2. 특정 게시글 조회
curl http://localhost:8080/api/posts/1

# 3. 새 게시글 생성
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Plain Java로 만든 REST API","content":"Spring 없이도 가능합니다!","authorId":1}'
```

## 핵심 코드 설명

### 1. HttpServer 설정

```java
HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
server.createContext("/api/users", new UserHandler());
server.createContext("/api/posts", new PostHandler());
server.setExecutor(null); // Default executor
server.start();
```

### 2. HTTP Handler 구조

```java
public class UserHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET" -> handleGet(exchange, path);
            case "POST" -> handlePost(exchange);
            case "PUT" -> handlePut(exchange, path);
            case "DELETE" -> handleDelete(exchange, path);
            default -> Response.sendError(exchange, 405, "Method Not Allowed");
        }
    }
}
```

### 3. JSON 직렬화 (수동 구현)

```java
public class JsonUtil {
    public static String toJson(User user) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}",
            user.getId(), user.getName(), user.getEmail()
        );
    }

    public static User fromJson(String json) {
        // 정규식 또는 수동 파싱
        // 실제로는 간단한 파서 구현
    }
}
```

### 4. 응답 헬퍼

```java
public class Response {
    public static void sendJson(HttpExchange exchange, int statusCode, String json) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}
```

## Spring과의 비교

| 기능 | Plain Java | Spring Boot |
|------|------------|-------------|
| 서버 시작 | HttpServer 수동 설정 | @SpringBootApplication 자동 설정 |
| 라우팅 | createContext() 수동 등록 | @GetMapping, @PostMapping |
| JSON 변환 | 수동 구현 | Jackson 자동 변환 |
| 의존성 주입 | new 키워드로 직접 생성 | @Autowired 자동 주입 |
| 예외 처리 | try-catch 수동 처리 | @ExceptionHandler |
| 설정 | 코드로 하드코딩 | application.properties |
| 개발 속도 | 느림 (모든 것 수동) | 빠름 (자동화) |
| 학습 가치 | HTTP 프로토콜 이해 | 비즈니스 로직 집중 |

## 장단점

### 장점 ✅

1. **의존성 없음**: JDK만 있으면 실행 가능
2. **가벼움**: 최소한의 메모리와 리소스 사용
3. **명확함**: 모든 동작이 명시적이고 투명함
4. **HTTP 이해**: 낮은 레벨에서 HTTP 요청/응답 처리 학습
5. **빠른 시작**: 무거운 프레임워크 없이 즉시 실행

### 단점 ❌

1. **보일러플레이트**: 반복적인 코드가 많음
2. **기능 부족**: 검증, 보안, 트랜잭션 등 직접 구현 필요
3. **생산성 낮음**: Spring의 자동화된 기능들 없음
4. **유지보수 어려움**: 프로젝트 규모 커지면 복잡도 증가
5. **에코시스템 부족**: Spring의 풍부한 라이브러리 활용 불가

## 학습 포인트

### 1. HTTP 프로토콜 이해

- HTTP 메서드 (GET, POST, PUT, DELETE)
- HTTP 헤더 (Content-Type, Accept)
- HTTP 상태 코드 (200, 201, 404, 500)
- Request Body 읽기
- Response Body 쓰기

### 2. 라우팅 패턴

- Path 파싱 (`/api/users/1`에서 ID 추출)
- 메서드별 분기 처리
- 컨텍스트 기반 핸들러 등록

### 3. JSON 처리

- 객체 → JSON 문자열 (직렬화)
- JSON 문자열 → 객체 (역직렬화)
- 이스케이프 처리
- 중첩 객체 처리

### 4. 메모리 데이터 관리

- In-memory 데이터 저장 (Map 사용)
- Thread-safety 고려 (ConcurrentHashMap)
- ID 자동 증가 (AtomicLong)

## 확장 아이디어

이 프로젝트를 확장하여 다음 기능을 추가해볼 수 있습니다:

- [ ] 실제 JSON 라이브러리 사용 (Gson, Jackson)
- [ ] 데이터베이스 연동 (JDBC)
- [ ] 예외 처리 및 에러 응답 표준화
- [ ] 로깅 (java.util.logging)
- [ ] CORS 헤더 추가
- [ ] 파일 업로드/다운로드
- [ ] JWT 인증
- [ ] Swagger/OpenAPI 문서
- [ ] 비동기 처리 (ExecutorService)
- [ ] 정적 파일 서빙 (HTML, CSS, JS)

## 언제 Plain Java를 사용할까?

### 사용하기 좋은 경우 ✅

- 마이크로 서비스 (극도로 가벼워야 할 때)
- 학습 목적 (HTTP 프로토콜 이해)
- 임베디드 시스템 (리소스 제약)
- 프로토타이핑 (빠른 PoC)

### Spring을 사용해야 하는 경우 ✅

- 엔터프라이즈 애플리케이션
- 복잡한 비즈니스 로직
- 보안/인증이 중요한 경우
- 팀 협업 프로젝트
- 장기 유지보수가 필요한 경우

## 참고 자료

- [HttpServer JavaDoc](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpServer.html)
- [RESTful API 설계 가이드](https://restfulapi.net/)
- [HTTP 상태 코드](https://developer.mozilla.org/ko/docs/Web/HTTP/Status)

---

**이 프로젝트를 통해 Spring이 얼마나 많은 것을 자동화해주는지 이해할 수 있습니다! 🚀**
