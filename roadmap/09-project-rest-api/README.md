# 09. 실전 프로젝트 1: RESTful API 서버

> **프로젝트 기간**: 2주
> **난이도**: ⭐⭐⭐⭐☆
> **전제 조건**: Phase 2 완료

## 🎯 프로젝트 목표

실무 수준의 RESTful API 서버를 처음부터 끝까지 구축합니다.

## 📋 프로젝트 요구사항

### 기능 요구사항

1. **사용자 관리**
   - 회원가입 (이메일 중복 검증)
   - 로그인 (JWT 토큰 발급)
   - 프로필 조회/수정
   - 비밀번호 변경

2. **게시판 CRUD**
   - 게시글 작성/수정/삭제
   - 게시글 목록 조회 (페이징, 정렬)
   - 게시글 검색 (제목, 내용)
   - 댓글 작성/삭제

3. **파일 업로드**
   - 이미지 업로드
   - 파일 다운로드
   - 파일 저장 (로컬 또는 S3)

### 기술 요구사항

- Spring Boot 3.x
- Spring Data JPA
- Spring Security + JWT
- MySQL/PostgreSQL
- Swagger/OpenAPI 문서화
- 단위 테스트 (JUnit 5, Mockito)
- 통합 테스트 (MockMvc)
- Docker 컨테이너화

## 🏗 프로젝트 구조

```
src/main/java/com/example/blog/
├── domain/
│   ├── user/
│   │   ├── User.java
│   │   ├── UserRepository.java
│   │   ├── UserService.java
│   │   └── UserController.java
│   ├── post/
│   │   ├── Post.java
│   │   ├── PostRepository.java
│   │   ├── PostService.java
│   │   └── PostController.java
│   └── comment/
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
├── common/
│   ├── dto/
│   ├── exception/
│   └── config/
└── BlogApplication.java
```

## 🛠 주요 구현 코드

### 1. Entity 설계

```java
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 2. Service Layer

```java
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public PostResponse createPost(CreatePostRequest request, String username) {
        User author = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = Post.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .author(author)
            .build();

        Post saved = postRepository.save(post);
        return PostResponse.from(saved);
    }

    public Page<PostResponse> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
            .map(PostResponse::from);
    }
}
```

### 3. API 문서화 (Swagger)

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Blog API")
                .version("1.0")
                .description("RESTful API for Blog"))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### 4. 테스트

```java
@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPost_Success() throws Exception {
        CreatePostRequest request = new CreatePostRequest("Title", "Content");

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Title"));
    }
}
```

## 📦 Docker 배포

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/blog-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: blogdb
    ports:
      - "3306:3306"

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/blogdb
```

## 📖 참고 자료

- [Spring Boot Best Practices](https://www.baeldung.com/spring-boot-best-practices)
- [REST API Design Guidelines](https://restfulapi.net/)

## ✅ 체크리스트

- [ ] 프로젝트 초기 설정
- [ ] Entity 설계 및 연관관계 매핑
- [ ] Repository 작성
- [ ] Service Layer 구현
- [ ] Controller 및 DTO 작성
- [ ] JWT 인증 구현
- [ ] 검증 로직 추가
- [ ] 예외 처리
- [ ] Swagger 문서화
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] Docker 컨테이너화
- [ ] README 작성

## 🚀 다음 단계

**→ [10. Spring WebFlux (반응형 프로그래밍)](../10-spring-webflux/)**
