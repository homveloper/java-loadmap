# 06. Spring Web MVC

> **학습 기간**: 1주
> **난이도**: ⭐⭐⭐☆☆
> **전제 조건**: Spring Boot 기초, Java 기본 문법

## 📚 학습 목표

Golang의 Gin/Echo 프레임워크 경험을 활용하여 Spring Web MVC로 RESTful API 서버를 구축합니다.

## 🎯 핵심 개념 비교: Spring MVC vs Golang Web Framework

| 기능 | Spring MVC | Golang (Gin/Echo) |
|------|------------|-------------------|
| **라우팅** | @GetMapping, @PostMapping | gin.GET(), echo.GET() |
| **컨트롤러** | @RestController | Handler Function |
| **요청 파싱** | @RequestBody, @PathVariable | c.BindJSON(), c.Param() |
| **미들웨어** | Filter, Interceptor | gin.Use(), echo.Use() |
| **검증** | @Valid, Bean Validation | go-validator |
| **에러 처리** | @ExceptionHandler, @ControllerAdvice | echo.HTTPErrorHandler |
| **DI** | @Autowired | 수동 주입 |

---

## 1. 첫 REST API 만들기

### 1.1 기본 Controller

**Spring MVC**:
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    public List<User> getAllUsers() {
        return List.of(
            new User(1L, "Alice", "alice@example.com"),
            new User(2L, "Bob", "bob@example.com")
        );
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return new User(id, "Alice", "alice@example.com");
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        // 저장 로직
        return user;
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return user;
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        // 삭제 로직
    }
}

record User(Long id, String name, String email) {}
```

**Golang (Gin) 비교**:
```go
func main() {
    r := gin.Default()

    r.GET("/api/users", func(c *gin.Context) {
        users := []User{
            {ID: 1, Name: "Alice", Email: "alice@example.com"},
            {ID: 2, Name: "Bob", Email: "bob@example.com"},
        }
        c.JSON(200, users)
    })

    r.GET("/api/users/:id", func(c *gin.Context) {
        id := c.Param("id")
        user := User{ID: 1, Name: "Alice", Email: "alice@example.com"}
        c.JSON(200, user)
    })

    r.POST("/api/users", func(c *gin.Context) {
        var user User
        c.BindJSON(&user)
        c.JSON(201, user)
    })

    r.Run(":8080")
}
```

### 1.2 어노테이션 상세

```java
// @RestController = @Controller + @ResponseBody
@RestController
@RequestMapping("/api/products")  // 클래스 레벨 경로
public class ProductController {

    // HTTP 메서드 매핑
    @GetMapping                    // GET /api/products
    @PostMapping                   // POST /api/products
    @PutMapping("/{id}")          // PUT /api/products/{id}
    @PatchMapping("/{id}")        // PATCH /api/products/{id}
    @DeleteMapping("/{id}")       // DELETE /api/products/{id}

    // 경로 변수
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }

    // 쿼리 파라미터
    @GetMapping
    public List<Product> getProducts(
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return productService.findAll(category, page, size);
    }

    // 요청 본문
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.save(product);
    }

    // 헤더 값
    @GetMapping("/secure")
    public String secureEndpoint(@RequestHeader("Authorization") String token) {
        return "Authorized";
    }

    // 쿠키 값
    @GetMapping("/session")
    public String session(@CookieValue("JSESSIONID") String sessionId) {
        return "Session: " + sessionId;
    }
}
```

---

## 2. 요청/응답 처리

### 2.1 DTO (Data Transfer Object)

```java
// 요청 DTO
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;

    // Getters, Setters
}

// 응답 DTO
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;

    // Getters, Setters
}

// Controller
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        User user = userService.create(request);
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
```

### 2.2 ResponseEntity로 HTTP 상태 제어

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);

        return user.map(u -> ResponseEntity.ok(mapToResponse(u)))
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.create(request);
        UserResponse response = mapToResponse(user);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/users/" + user.getId())
            .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Golang (Gin) 비교**:
```go
r.GET("/api/users/:id", func(c *gin.Context) {
    id := c.Param("id")
    user, err := userService.FindByID(id)

    if err != nil {
        c.JSON(404, gin.H{"error": "User not found"})
        return
    }

    c.JSON(200, user)
})

r.POST("/api/users", func(c *gin.Context) {
    var req CreateUserRequest
    if err := c.BindJSON(&req); err != nil {
        c.JSON(400, gin.H{"error": err.Error()})
        return
    }

    user := userService.Create(req)
    c.Header("Location", "/api/users/" + user.ID)
    c.JSON(201, user)
})
```

---

## 3. 데이터 검증 (Validation)

### 3.1 Bean Validation

```java
import jakarta.validation.constraints.*;

// DTO with validation annotations
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
             message = "Password must contain at least one digit, one lowercase and one uppercase letter")
    private String password;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must not exceed 100")
    private int age;

    // Getters, Setters
}

// Controller
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // @Valid가 검증 실패 시 자동으로 400 Bad Request 응답
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(user));
    }
}
```

### 3.2 커스텀 Validator

```java
// 커스텀 어노테이션
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
public @interface UniqueUsername {
    String message() default "Username already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator 구현
@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null) {
            return true;
        }
        return !userRepository.existsByUsername(username);
    }
}

// 사용
public class CreateUserRequest {
    @UniqueUsername
    private String username;
}
```

---

## 4. 예외 처리

### 4.1 @ExceptionHandler

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // 컨트롤러 내 예외 처리
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
```

### 4.2 @ControllerAdvice (전역 예외 처리)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 특정 예외 처리
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors,
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

record ValidationErrorResponse(
    int status,
    String message,
    Map<String, String> errors,
    LocalDateTime timestamp
) {}
```

**Golang (Gin) 비교**:
```go
// Gin 에러 핸들링
r.Use(func(c *gin.Context) {
    defer func() {
        if err := recover(); err != nil {
            c.JSON(500, gin.H{
                "status":  500,
                "message": "Internal server error",
            })
        }
    }()
    c.Next()
})

// 커스텀 에러
r.GET("/api/users/:id", func(c *gin.Context) {
    user, err := userService.FindByID(c.Param("id"))
    if err != nil {
        c.JSON(404, gin.H{
            "status":  404,
            "message": "User not found",
        })
        return
    }
    c.JSON(200, user)
})
```

---

## 5. 필터와 인터셉터 (Middleware)

### 5.1 Filter

```java
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        // 요청 로깅
        System.out.println("Request: " + httpRequest.getMethod() + " " +
                           httpRequest.getRequestURI());

        chain.doFilter(request, response);

        // 응답 로깅
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Response: " + httpResponse.getStatus() +
                           " (" + duration + "ms)");
    }
}
```

### 5.2 Interceptor

```java
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || !isValidToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false;  // 요청 중단
        }

        return true;  // 요청 계속 진행
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) throws Exception {
        // 컨트롤러 실행 후, 뷰 렌더링 전
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 뷰 렌더링 후 (정리 작업)
    }

    private boolean isValidToken(String token) {
        // 토큰 검증 로직
        return token.startsWith("Bearer ");
    }
}

// Interceptor 등록
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }
}
```

**Golang (Gin) 비교**:
```go
// Gin Middleware
func LoggingMiddleware() gin.HandlerFunc {
    return func(c *gin.Context) {
        startTime := time.Now()

        // 요청 로깅
        fmt.Printf("Request: %s %s\n", c.Request.Method, c.Request.URL.Path)

        c.Next()  // 다음 핸들러 실행

        // 응답 로깅
        duration := time.Since(startTime)
        fmt.Printf("Response: %d (%v)\n", c.Writer.Status(), duration)
    }
}

func AuthMiddleware() gin.HandlerFunc {
    return func(c *gin.Context) {
        token := c.GetHeader("Authorization")

        if token == "" || !isValidToken(token) {
            c.JSON(401, gin.H{"error": "Unauthorized"})
            c.Abort()
            return
        }

        c.Next()
    }
}

// 사용
r := gin.Default()
r.Use(LoggingMiddleware())

api := r.Group("/api")
api.Use(AuthMiddleware())
api.GET("/users", getUsers)
```

**Filter vs Interceptor 차이**:
- **Filter**: Servlet 레벨, Spring 외부에서 동작
- **Interceptor**: Spring MVC 레벨, Controller 전후 동작

---

## 6. CORS 설정

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "https://example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

// 또는 어노테이션 방식
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    // ...
}
```

---

## 7. 페이징과 정렬

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public Page<ProductResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return productService.findAll(pageable)
                .map(this::mapToResponse);
    }

    // Spring Data JPA는 자동으로 Page 객체 반환
}

// Response 예시
/*
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "first": true
}
*/
```

---

## 8. 명시적 라우팅 (Functional Endpoints)

### 8.1 어노테이션 vs 함수형 라우팅 비교

Spring에서는 어노테이션 방식 외에도 **함수형 엔드포인트(Functional Endpoints)**를 사용하여 명시적으로 라우트를 정의할 수 있습니다.

| 방식 | 어노테이션 기반 | 함수형 엔드포인트 |
|------|----------------|------------------|
| **정의 방법** | @RestController, @GetMapping | RouterFunction, HandlerFunction |
| **타입** | 선언적(Declarative) | 명시적(Explicit) |
| **장점** | 간단하고 직관적 | 프로그래매틱, 동적 라우팅 가능 |
| **유연성** | 낮음 | 높음 |

### 8.2 기본 함수형 엔드포인트

**어노테이션 방식**:
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
}
```

**함수형 엔드포인트 방식**:
```java
// Handler 클래스
@Component
public class UserHandler {

    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    // HandlerFunction 정의
    public Mono<ServerResponse> getUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        User user = userService.findById(id);

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(user);
    }

    public Mono<ServerResponse> createUser(ServerRequest request) {
        User user = request.bodyToMono(User.class).block();
        User saved = userService.save(user);

        return ServerResponse.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(saved);
    }

    public Mono<ServerResponse> listUsers(ServerRequest request) {
        List<User> users = userService.findAll();

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(users);
    }
}

// Router 설정
@Configuration
public class UserRouter {

    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return RouterFunctions
            .route(GET("/api/users/{id}"), handler::getUser)
            .andRoute(GET("/api/users"), handler::listUsers)
            .andRoute(POST("/api/users"), handler::createUser);
    }
}
```

### 8.3 고급 라우팅 패턴

```java
@Configuration
public class ApiRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(
            UserHandler userHandler,
            PostHandler postHandler) {

        return RouterFunctions
            // 중첩된 라우트 (Nested Routes)
            .nest(path("/api"),
                RouterFunctions
                    // User 라우트
                    .nest(path("/users"),
                        route(GET(""), userHandler::listUsers)
                            .andRoute(GET("/{id}"), userHandler::getUser)
                            .andRoute(POST(""), userHandler::createUser)
                            .andRoute(PUT("/{id}"), userHandler::updateUser)
                            .andRoute(DELETE("/{id}"), userHandler::deleteUser))

                    // Post 라우트
                    .andNest(path("/posts"),
                        route(GET(""), postHandler::listPosts)
                            .andRoute(POST(""), postHandler::createPost))
            )

            // 조건부 라우팅 (Predicate)
            .andRoute(GET("/api/users/{id}")
                .and(accept(MediaType.APPLICATION_JSON)),
                userHandler::getUser)

            // 헤더 조건
            .andRoute(GET("/api/admin/users")
                .and(headers(h -> h.header("X-Admin-Token").contains("secret"))),
                userHandler::adminListUsers)

            // 쿼리 파라미터 조건
            .andRoute(GET("/api/users/search")
                .and(queryParam("name", name -> !name.isEmpty())),
                userHandler::searchUsers);
    }
}
```

### 8.4 요청/응답 처리

```java
@Component
public class ProductHandler {

    private final ProductService productService;

    // 경로 변수 추출
    public Mono<ServerResponse> getProduct(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        Optional<Product> product = productService.findById(id);

        return product
            .map(p -> ServerResponse.ok().body(p))
            .orElse(ServerResponse.notFound().build());
    }

    // 쿼리 파라미터 추출
    public Mono<ServerResponse> searchProducts(ServerRequest request) {
        String category = request.queryParam("category").orElse("");
        int page = request.queryParam("page")
            .map(Integer::parseInt)
            .orElse(0);
        int size = request.queryParam("size")
            .map(Integer::parseInt)
            .orElse(10);

        List<Product> products = productService.search(category, page, size);

        return ServerResponse.ok().body(products);
    }

    // 요청 본문 처리
    public Mono<ServerResponse> createProduct(ServerRequest request) {
        return request.bodyToMono(CreateProductRequest.class)
            .map(productService::create)
            .flatMap(product -> ServerResponse
                .status(HttpStatus.CREATED)
                .header("Location", "/api/products/" + product.getId())
                .body(product))
            .onErrorResume(e -> ServerResponse
                .badRequest()
                .body(Map.of("error", e.getMessage())));
    }

    // 헤더 읽기
    public Mono<ServerResponse> secureEndpoint(ServerRequest request) {
        String token = request.headers()
            .firstHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ServerResponse.ok().body("Authorized");
    }
}
```

### 8.5 필터와 예외 처리

```java
@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(UserHandler handler) {
        return RouterFunctions
            .route(GET("/api/users/{id}"), handler::getUser)
            .andRoute(POST("/api/users"), handler::createUser)

            // 라우트별 필터 적용
            .filter((request, next) -> {
                System.out.println("Request: " + request.method() + " " + request.uri());
                long startTime = System.currentTimeMillis();

                return next.handle(request)
                    .doOnNext(response -> {
                        long duration = System.currentTimeMillis() - startTime;
                        System.out.println("Response: " + response.statusCode() +
                                         " (" + duration + "ms)");
                    });
            })

            // 에러 핸들링
            .filter((request, next) ->
                next.handle(request)
                    .onErrorResume(ResourceNotFoundException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", e.getMessage())))
                    .onErrorResume(ValidationException.class, e ->
                        ServerResponse.badRequest()
                            .body(Map.of("error", e.getMessage())))
                    .onErrorResume(Exception.class, e ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Internal server error")))
            );
    }
}
```

### 8.6 Golang 스타일 라우팅 비교

**Golang (Gin)**:
```go
func SetupRoutes(r *gin.Engine) {
    api := r.Group("/api")
    {
        users := api.Group("/users")
        {
            users.GET("", listUsers)
            users.GET("/:id", getUser)
            users.POST("", createUser)
            users.PUT("/:id", updateUser)
            users.DELETE("/:id", deleteUser)
        }

        posts := api.Group("/posts")
        {
            posts.GET("", listPosts)
            posts.POST("", createPost)
        }
    }
}

func getUser(c *gin.Context) {
    id := c.Param("id")
    user, err := userService.FindByID(id)

    if err != nil {
        c.JSON(404, gin.H{"error": "User not found"})
        return
    }

    c.JSON(200, user)
}
```

**Spring 함수형 엔드포인트**:
```java
@Bean
public RouterFunction<ServerResponse> routes(
        UserHandler userHandler,
        PostHandler postHandler) {

    return nest(path("/api"),
        nest(path("/users"),
            route(GET(""), userHandler::listUsers)
                .andRoute(GET("/{id}"), userHandler::getUser)
                .andRoute(POST(""), userHandler::createUser)
                .andRoute(PUT("/{id}"), userHandler::updateUser)
                .andRoute(DELETE("/{id}"), userHandler::deleteUser))
        .andNest(path("/posts"),
            route(GET(""), postHandler::listPosts)
                .andRoute(POST(""), postHandler::createPost))
    );
}
```

### 8.7 완전한 예시: 블로그 API (함수형 스타일)

```java
// Handler
@Component
public class BlogHandler {

    private final PostService postService;

    public BlogHandler(PostService postService) {
        this.postService = postService;
    }

    public Mono<ServerResponse> listPosts(ServerRequest request) {
        int page = request.queryParam("page")
            .map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size")
            .map(Integer::parseInt).orElse(10);

        Page<Post> posts = postService.findAll(PageRequest.of(page, size));

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(posts);
    }

    public Mono<ServerResponse> getPost(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));

        return postService.findById(id)
            .map(post -> ServerResponse.ok().body(post))
            .orElse(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createPost(ServerRequest request) {
        return request.bodyToMono(CreatePostRequest.class)
            .map(postService::create)
            .flatMap(post -> ServerResponse
                .status(HttpStatus.CREATED)
                .body(post));
    }

    public Mono<ServerResponse> updatePost(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));

        return request.bodyToMono(UpdatePostRequest.class)
            .map(req -> postService.update(id, req))
            .flatMap(post -> ServerResponse.ok().body(post))
            .onErrorResume(ResourceNotFoundException.class,
                e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deletePost(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        postService.delete(id);

        return ServerResponse.noContent().build();
    }
}

// Router Configuration
@Configuration
public class BlogRouter {

    @Bean
    public RouterFunction<ServerResponse> blogRoutes(BlogHandler handler) {
        return RouterFunctions
            .nest(path("/api/posts"),
                route(GET(""), handler::listPosts)
                    .andRoute(GET("/{id}"), handler::getPost)
                    .andRoute(POST(""), handler::createPost)
                    .andRoute(PUT("/{id}"), handler::updatePost)
                    .andRoute(DELETE("/{id}"), handler::deletePost)
            )
            // 로깅 필터
            .filter((request, next) -> {
                System.out.println("Request: " + request.method() + " " + request.uri());
                return next.handle(request);
            });
    }
}
```

### 8.8 언제 함수형 엔드포인트를 사용할까?

**어노테이션 방식을 사용하는 경우**:
- 간단한 CRUD API
- 팀이 어노테이션 방식에 익숙한 경우
- 빠른 개발이 필요한 경우

**함수형 엔드포인트를 사용하는 경우**:
- 동적으로 라우트를 생성해야 하는 경우
- 복잡한 라우팅 조건이 필요한 경우
- 함수형 프로그래밍 스타일을 선호하는 경우
- WebFlux와 반응형 프로그래밍을 사용하는 경우

**혼용 가능**: 두 방식을 같은 프로젝트에서 함께 사용할 수 있습니다!

---

## 🛠 실습 프로젝트: 간단한 블로그 API

```java
// Entity
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor(String author);
}

// Service
@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public Post create(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public Post update(Long id, Post post) {
        Post existing = postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        existing.setTitle(post.getTitle());
        existing.setContent(post.getContent());
        existing.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(existing);
    }

    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}

// Controller
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        return postService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody Post post) {
        Post created = postService.create(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id,
                                           @Valid @RequestBody Post post) {
        Post updated = postService.update(id, post);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 📖 학습 리소스

- [Spring MVC 공식 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- [김영한 - 스프링 MVC 1편](https://www.inflearn.com/course/스프링-mvc-1)
- [김영한 - 스프링 MVC 2편](https://www.inflearn.com/course/스프링-mvc-2)

---

## ✅ 체크리스트

- [ ] @RestController, @RequestMapping 이해
- [ ] HTTP 메서드 매핑 (@GetMapping, @PostMapping 등)
- [ ] 요청 파라미터 처리 (@PathVariable, @RequestParam, @RequestBody)
- [ ] ResponseEntity로 HTTP 응답 제어
- [ ] Bean Validation (@Valid, @NotBlank 등)
- [ ] @ExceptionHandler, @ControllerAdvice
- [ ] Filter와 Interceptor 차이
- [ ] CORS 설정
- [ ] 페이징과 정렬
- [ ] 함수형 엔드포인트 (RouterFunction, HandlerFunction)
- [ ] 명시적 라우팅과 어노테이션 방식 비교
- [ ] 실습 프로젝트 (블로그 API) 완료

---

## 🚀 다음 단계

**→ [07. Spring Data JPA & Database](../07-spring-data-jpa/)**
