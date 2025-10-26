package com.example.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.servlet.function.RequestPredicates.*;
import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * 완전한 블로그 API 애플리케이션 - 단일 파일 버전
 *
 * 이 파일은 README의 블로그 API 예제를 실제로 동작하는 단일 Java 파일로 구현한 것입니다.
 *
 * 포함된 컴포넌트:
 * - Spring Boot Application
 * - JPA Entity (Post)
 * - Repository (PostRepository)
 * - Service (PostService)
 * - REST Controller (PostController) - 어노테이션 방식
 * - Functional Endpoints (PostHandler, PostRouter) - 명시적 라우팅
 * - JSON-RPC Controller (JsonRpcController) - JSON-RPC over HTTP
 * - gRPC Service (BlogGrpcService) - gRPC 바인딩
 * - Global Exception Handler
 * - Custom Exceptions
 * - DTOs (CreatePostRequest, UpdatePostRequest)
 *
 * 실행 방법:
 * mvn spring-boot:run
 *
 * API 엔드포인트:
 *
 * [어노테이션 방식 - /api/posts]
 * GET    /api/posts        - 모든 게시글 조회
 * GET    /api/posts/{id}   - 특정 게시글 조회
 * POST   /api/posts        - 새 게시글 생성
 * PUT    /api/posts/{id}   - 게시글 수정
 * DELETE /api/posts/{id}   - 게시글 삭제
 *
 * [Functional Endpoints - /functional/posts]
 * GET    /functional/posts        - 모든 게시글 조회
 * GET    /functional/posts/{id}   - 특정 게시글 조회
 * POST   /functional/posts        - 새 게시글 생성
 * PUT    /functional/posts/{id}   - 게시글 수정
 * DELETE /functional/posts/{id}   - 게시글 삭제
 *
 * [JSON-RPC - POST /jsonrpc]
 * post.list     - 모든 게시글 조회
 * post.get      - 특정 게시글 조회
 * post.create   - 새 게시글 생성
 * post.update   - 게시글 수정
 * post.delete   - 게시글 삭제
 *
 * [gRPC - port 9090]
 * ListPosts    - 모든 게시글 조회
 * GetPost      - 특정 게시글 조회
 * CreatePost   - 새 게시글 생성
 * UpdatePost   - 게시글 수정
 * DeletePost   - 게시글 삭제
 */
@SpringBootApplication
public class BlogApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApiApplication.class, args);
    }
}

// ============================================================
// Entity
// ============================================================

@Entity
@Table(name = "posts")
class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // Constructors
    public Post() {
    }

    public Post(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// ============================================================
// Repository
// ============================================================

interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor(String author);
    List<Post> findByTitleContaining(String title);
}

// ============================================================
// Service
// ============================================================

@Service
class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> findByAuthor(String author) {
        return postRepository.findByAuthor(author);
    }

    public Post create(CreatePostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(request.getAuthor());
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public Post update(Long id, UpdatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }
}

// ============================================================
// Controller
// ============================================================

@RestController
@RequestMapping("/api/posts")
class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts(
            @RequestParam(required = false) String author) {
        List<Post> posts;
        if (author != null && !author.isEmpty()) {
            posts = postService.findByAuthor(author);
        } else {
            posts = postService.findAll();
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        return postService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest request) {
        Post created = postService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/posts/" + created.getId())
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request) {
        Post updated = postService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

// ============================================================
// Functional Endpoints (명시적 라우팅)
// ============================================================

/**
 * PostHandler - Functional Endpoints를 위한 핸들러
 *
 * 어노테이션 방식(@RestController)과 달리, 명시적으로 요청을 처리하는 핸들러 함수들입니다.
 * Golang의 Gin/Echo 프레임워크의 핸들러 함수와 유사한 패턴입니다.
 */
@Component
class PostHandler {

    private final PostService postService;

    public PostHandler(PostService postService) {
        this.postService = postService;
    }

    /**
     * 모든 게시글 조회
     */
    public ServerResponse listPosts(ServerRequest request) {
        try {
            // 쿼리 파라미터에서 author 추출
            Optional<String> author = request.param("author");

            List<Post> posts;
            if (author.isPresent() && !author.get().isEmpty()) {
                posts = postService.findByAuthor(author.get());
            } else {
                posts = postService.findAll();
            }

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(posts);
        } catch (Exception e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 특정 게시글 조회
     */
    public ServerResponse getPost(ServerRequest request) {
        try {
            Long id = Long.parseLong(request.pathVariable("id"));
            Optional<Post> post = postService.findById(id);

            return post.map(p -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(p))
                    .orElseGet(() -> ServerResponse.notFound().build());
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest()
                    .body(Map.of("error", "Invalid ID format"));
        } catch (Exception e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 새 게시글 생성
     */
    public ServerResponse createPost(ServerRequest request) {
        try {
            CreatePostRequest createRequest = request.body(CreatePostRequest.class);

            // 간단한 검증
            if (createRequest.getTitle() == null || createRequest.getTitle().isBlank()) {
                return ServerResponse.badRequest()
                        .body(Map.of("error", "Title is required"));
            }
            if (createRequest.getContent() == null || createRequest.getContent().isBlank()) {
                return ServerResponse.badRequest()
                        .body(Map.of("error", "Content is required"));
            }
            if (createRequest.getAuthor() == null || createRequest.getAuthor().isBlank()) {
                return ServerResponse.badRequest()
                        .body(Map.of("error", "Author is required"));
            }

            Post created = postService.create(createRequest);

            return ServerResponse.created(URI.create("/functional/posts/" + created.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(created);
        } catch (Exception e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 게시글 수정
     */
    public ServerResponse updatePost(ServerRequest request) {
        try {
            Long id = Long.parseLong(request.pathVariable("id"));
            UpdatePostRequest updateRequest = request.body(UpdatePostRequest.class);

            Post updated = postService.update(id, updateRequest);

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(updated);
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest()
                    .body(Map.of("error", "Invalid ID format"));
        } catch (ResourceNotFoundException e) {
            return ServerResponse.notFound().build();
        } catch (Exception e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 게시글 삭제
     */
    public ServerResponse deletePost(ServerRequest request) {
        try {
            Long id = Long.parseLong(request.pathVariable("id"));
            postService.delete(id);

            return ServerResponse.noContent().build();
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest()
                    .body(Map.of("error", "Invalid ID format"));
        } catch (ResourceNotFoundException e) {
            return ServerResponse.notFound().build();
        } catch (Exception e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

/**
 * PostRouter - Functional Endpoints 라우터 설정
 *
 * 명시적으로 라우트를 정의합니다. Golang의 라우터 설정과 유사한 패턴입니다.
 *
 * 비교:
 * [Golang - Gin]
 * r.GET("/posts", handler.ListPosts)
 * r.GET("/posts/:id", handler.GetPost)
 *
 * [Spring - Functional Endpoints]
 * route(GET("/posts"), handler::listPosts)
 * route(GET("/posts/{id}"), handler::getPost)
 */
@Configuration
class PostRouter {

    @Bean
    public RouterFunction<ServerResponse> functionalPostRoutes(PostHandler handler) {
        return route(GET("/functional/posts"), handler::listPosts)
                .andRoute(GET("/functional/posts/{id}"), handler::getPost)
                .andRoute(POST("/functional/posts"), handler::createPost)
                .andRoute(PUT("/functional/posts/{id}"), handler::updatePost)
                .andRoute(DELETE("/functional/posts/{id}"), handler::deletePost)
                // 요청/응답 로깅 필터
                .filter((request, next) -> {
                    System.out.println("[Functional] " + request.method() + " " + request.uri());
                    long startTime = System.currentTimeMillis();

                    ServerResponse response = next.handle(request);

                    long duration = System.currentTimeMillis() - startTime;
                    System.out.println("[Functional] Response: " + response.statusCode() +
                            " (" + duration + "ms)");

                    return response;
                });
    }
}

// ============================================================
// DTOs (Data Transfer Objects)
// ============================================================

class CreatePostRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
    private String content;

    @NotBlank(message = "Author is required")
    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    private String author;

    // Constructors
    public CreatePostRequest() {
    }

    public CreatePostRequest(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}

class UpdatePostRequest {
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
    private String content;

    // Constructors
    public UpdatePostRequest() {
    }

    public UpdatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

// ============================================================
// Exception Classes
// ============================================================

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters
    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

class ValidationErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    public ValidationErrorResponse(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
    }

    // Getters
    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

// ============================================================
// Global Exception Handler
// ============================================================

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

// ============================================================
// gRPC Service (RPC 바인딩)
// ============================================================

/**
 * BlogGrpcService - gRPC 서비스 구현
 *
 * REST API와 동일한 기능을 gRPC로 제공합니다.
 * gRPC는 HTTP/2 기반의 고성능 RPC 프레임워크입니다.
 *
 * 사용 방법:
 * 1. grpcurl을 설치: brew install grpcurl (Mac) 또는 https://github.com/fullstorydev/grpcurl
 * 2. 서비스 목록 확인: grpcurl -plaintext localhost:9090 list
 * 3. 메서드 호출:
 *    grpcurl -plaintext -d '{"title":"제목","content":"내용","author":"작성자"}' \
 *      localhost:9090 blog.BlogService/CreatePost
 *
 * REST vs gRPC 비교:
 * - REST: HTTP/1.1, JSON, 텍스트 기반, 브라우저 친화적
 * - gRPC: HTTP/2, Protobuf, 바이너리, 고성능, 타입 안전성
 *
 * 언제 gRPC를 사용할까?
 * - 마이크로서비스 간 통신 (내부 API)
 * - 고성능이 필요한 경우
 * - 양방향 스트리밍이 필요한 경우
 * - 다국어 클라이언트 지원 (proto 파일로 자동 생성)
 */
@GrpcService
class BlogGrpcService extends BlogServiceGrpc.BlogServiceImplBase {

    private final PostService postService;

    public BlogGrpcService(PostService postService) {
        this.postService = postService;
    }

    /**
     * 모든 게시글 조회
     */
    @Override
    public void listPosts(BlogProto.ListPostsRequest request,
                          StreamObserver<BlogProto.ListPostsResponse> responseObserver) {
        try {
            List<Post> posts;

            // author 필터가 있으면 해당 작성자의 게시글만 조회
            if (request.hasAuthor() && !request.getAuthor().isEmpty()) {
                posts = postService.findByAuthor(request.getAuthor());
            } else {
                posts = postService.findAll();
            }

            // Post 엔티티를 Proto 메시지로 변환
            List<BlogProto.Post> protoPosts = posts.stream()
                    .map(this::toProtoPost)
                    .toList();

            BlogProto.ListPostsResponse response = BlogProto.ListPostsResponse.newBuilder()
                    .addAllPosts(protoPosts)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to list posts: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * 특정 게시글 조회
     */
    @Override
    public void getPost(BlogProto.GetPostRequest request,
                        StreamObserver<BlogProto.Post> responseObserver) {
        try {
            Long id = request.getId();
            Optional<Post> post = postService.findById(id);

            if (post.isPresent()) {
                BlogProto.Post protoPost = toProtoPost(post.get());
                responseObserver.onNext(protoPost);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Post not found with id: " + id)
                        .asRuntimeException());
            }
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to get post: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * 새 게시글 생성
     */
    @Override
    public void createPost(BlogProto.CreatePostRequest request,
                           StreamObserver<BlogProto.Post> responseObserver) {
        try {
            // 검증
            if (request.getTitle().isBlank()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Title is required")
                        .asRuntimeException());
                return;
            }
            if (request.getContent().isBlank()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Content is required")
                        .asRuntimeException());
                return;
            }
            if (request.getAuthor().isBlank()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Author is required")
                        .asRuntimeException());
                return;
            }

            CreatePostRequest createRequest = new CreatePostRequest(
                    request.getTitle(),
                    request.getContent(),
                    request.getAuthor()
            );

            Post created = postService.create(createRequest);
            BlogProto.Post protoPost = toProtoPost(created);

            responseObserver.onNext(protoPost);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to create post: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * 게시글 수정
     */
    @Override
    public void updatePost(BlogProto.UpdatePostRequest request,
                           StreamObserver<BlogProto.Post> responseObserver) {
        try {
            Long id = request.getId();

            UpdatePostRequest updateRequest = new UpdatePostRequest(
                    request.hasTitle() ? request.getTitle() : null,
                    request.hasContent() ? request.getContent() : null
            );

            Post updated = postService.update(id, updateRequest);
            BlogProto.Post protoPost = toProtoPost(updated);

            responseObserver.onNext(protoPost);
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to update post: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * 게시글 삭제
     */
    @Override
    public void deletePost(BlogProto.DeletePostRequest request,
                           StreamObserver<BlogProto.DeletePostResponse> responseObserver) {
        try {
            Long id = request.getId();
            postService.delete(id);

            BlogProto.DeletePostResponse response = BlogProto.DeletePostResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Post deleted successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to delete post: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Post 엔티티를 Proto 메시지로 변환
     */
    private BlogProto.Post toProtoPost(Post post) {
        BlogProto.Post.Builder builder = BlogProto.Post.newBuilder()
                .setId(post.getId())
                .setTitle(post.getTitle())
                .setContent(post.getContent())
                .setAuthor(post.getAuthor())
                .setCreatedAt(post.getCreatedAt().toString());

        if (post.getUpdatedAt() != null) {
            builder.setUpdatedAt(post.getUpdatedAt().toString());
        }

        return builder.build();
    }
}

// ============================================================
// JSON-RPC over HTTP (RPC 바인딩)
// ============================================================

/**
 * JSON-RPC 2.0 프로토콜 구현
 *
 * JSON-RPC는 경량 RPC 프로토콜로, HTTP POST를 통해 JSON 형식으로 원격 프로시저를 호출합니다.
 *
 * 프로토콜 사양:
 * - 요청: {"jsonrpc":"2.0", "method":"methodName", "params":{...}, "id":1}
 * - 응답: {"jsonrpc":"2.0", "result":{...}, "id":1}
 * - 에러: {"jsonrpc":"2.0", "error":{"code":-32600, "message":"..."}, "id":1}
 *
 * 사용 방법:
 * curl -X POST http://localhost:8080/jsonrpc \
 *   -H "Content-Type: application/json" \
 *   -d '{"jsonrpc":"2.0","method":"post.list","params":{},"id":1}'
 *
 * 지원 메서드:
 * - post.list      - 모든 게시글 조회
 * - post.get       - 특정 게시글 조회
 * - post.create    - 새 게시글 생성
 * - post.update    - 게시글 수정
 * - post.delete    - 게시글 삭제
 *
 * REST vs JSON-RPC vs gRPC 비교:
 * - REST: 리소스 중심, HTTP 메서드 활용, 캐싱 가능
 * - JSON-RPC: 메서드 중심, 단일 엔드포인트, 간단한 RPC
 * - gRPC: 고성능, 바이너리, HTTP/2, 타입 안전성
 */

/**
 * JSON-RPC 요청 DTO
 */
class JsonRpcRequest {
    private String jsonrpc = "2.0";
    private String method;
    private Object params;
    private Object id;

    // Constructors
    public JsonRpcRequest() {
    }

    public JsonRpcRequest(String method, Object params, Object id) {
        this.method = method;
        this.params = params;
        this.id = id;
    }

    // Getters and Setters
    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }
}

/**
 * JSON-RPC 응답 DTO
 */
class JsonRpcResponse {
    private String jsonrpc = "2.0";
    private Object result;
    private JsonRpcError error;
    private Object id;

    // Constructors
    public JsonRpcResponse() {
    }

    public JsonRpcResponse(Object result, Object id) {
        this.result = result;
        this.id = id;
    }

    public JsonRpcResponse(JsonRpcError error, Object id) {
        this.error = error;
        this.id = id;
    }

    // Getters and Setters
    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JsonRpcError getError() {
        return error;
    }

    public void setError(JsonRpcError error) {
        this.error = error;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }
}

/**
 * JSON-RPC 에러 DTO
 */
class JsonRpcError {
    private int code;
    private String message;
    private Object data;

    // JSON-RPC 표준 에러 코드
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;

    // 커스텀 에러 코드
    public static final int RESOURCE_NOT_FOUND = -32001;
    public static final int VALIDATION_ERROR = -32002;

    // Constructors
    public JsonRpcError() {
    }

    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JsonRpcError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

/**
 * JSON-RPC 컨트롤러
 *
 * 단일 엔드포인트(/jsonrpc)로 모든 RPC 호출을 처리합니다.
 * method 필드를 기반으로 적절한 서비스 메서드를 호출합니다.
 */
@RestController
@RequestMapping("/jsonrpc")
class JsonRpcController {

    private final PostService postService;

    public JsonRpcController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<JsonRpcResponse> handleJsonRpc(@RequestBody JsonRpcRequest request) {
        try {
            // 프로토콜 버전 검증
            if (!"2.0".equals(request.getJsonrpc())) {
                return ResponseEntity.ok(new JsonRpcResponse(
                        new JsonRpcError(JsonRpcError.INVALID_REQUEST, "Invalid JSON-RPC version"),
                        request.getId()
                ));
            }

            // 메서드 검증
            if (request.getMethod() == null || request.getMethod().isEmpty()) {
                return ResponseEntity.ok(new JsonRpcResponse(
                        new JsonRpcError(JsonRpcError.INVALID_REQUEST, "Method is required"),
                        request.getId()
                ));
            }

            // 메서드 라우팅
            Object result = routeMethod(request.getMethod(), request.getParams());

            return ResponseEntity.ok(new JsonRpcResponse(result, request.getId()));

        } catch (JsonRpcMethodNotFoundException e) {
            return ResponseEntity.ok(new JsonRpcResponse(
                    new JsonRpcError(JsonRpcError.METHOD_NOT_FOUND, e.getMessage()),
                    request.getId()
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.ok(new JsonRpcResponse(
                    new JsonRpcError(JsonRpcError.RESOURCE_NOT_FOUND, e.getMessage()),
                    request.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new JsonRpcResponse(
                    new JsonRpcError(JsonRpcError.INVALID_PARAMS, e.getMessage()),
                    request.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(new JsonRpcResponse(
                    new JsonRpcError(JsonRpcError.INTERNAL_ERROR, "Internal error: " + e.getMessage()),
                    request.getId()
            ));
        }
    }

    /**
     * 메서드 이름을 기반으로 적절한 서비스 메서드를 호출합니다.
     */
    private Object routeMethod(String method, Object params) {
        switch (method) {
            case "post.list":
                return handlePostList(params);

            case "post.get":
                return handlePostGet(params);

            case "post.create":
                return handlePostCreate(params);

            case "post.update":
                return handlePostUpdate(params);

            case "post.delete":
                return handlePostDelete(params);

            default:
                throw new JsonRpcMethodNotFoundException("Method not found: " + method);
        }
    }

    /**
     * post.list - 모든 게시글 조회
     * params: {"author": "작성자"} (선택적)
     */
    private Object handlePostList(Object params) {
        if (params instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paramsMap = (Map<String, Object>) params;
            String author = (String) paramsMap.get("author");

            if (author != null && !author.isEmpty()) {
                return postService.findByAuthor(author);
            }
        }
        return postService.findAll();
    }

    /**
     * post.get - 특정 게시글 조회
     * params: {"id": 1}
     */
    private Object handlePostGet(Object params) {
        if (!(params instanceof Map)) {
            throw new IllegalArgumentException("Invalid params: expected object with 'id' field");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramsMap = (Map<String, Object>) params;
        Object idObj = paramsMap.get("id");

        if (idObj == null) {
            throw new IllegalArgumentException("Missing required parameter: id");
        }

        Long id = convertToLong(idObj);
        return postService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    /**
     * post.create - 새 게시글 생성
     * params: {"title": "제목", "content": "내용", "author": "작성자"}
     */
    private Object handlePostCreate(Object params) {
        if (!(params instanceof Map)) {
            throw new IllegalArgumentException("Invalid params: expected object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramsMap = (Map<String, Object>) params;

        String title = (String) paramsMap.get("title");
        String content = (String) paramsMap.get("content");
        String author = (String) paramsMap.get("author");

        // 검증
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author is required");
        }

        CreatePostRequest request = new CreatePostRequest(title, content, author);
        return postService.create(request);
    }

    /**
     * post.update - 게시글 수정
     * params: {"id": 1, "title": "수정된 제목", "content": "수정된 내용"}
     */
    private Object handlePostUpdate(Object params) {
        if (!(params instanceof Map)) {
            throw new IllegalArgumentException("Invalid params: expected object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramsMap = (Map<String, Object>) params;

        Object idObj = paramsMap.get("id");
        if (idObj == null) {
            throw new IllegalArgumentException("Missing required parameter: id");
        }

        Long id = convertToLong(idObj);
        String title = (String) paramsMap.get("title");
        String content = (String) paramsMap.get("content");

        UpdatePostRequest request = new UpdatePostRequest(title, content);
        return postService.update(id, request);
    }

    /**
     * post.delete - 게시글 삭제
     * params: {"id": 1}
     */
    private Object handlePostDelete(Object params) {
        if (!(params instanceof Map)) {
            throw new IllegalArgumentException("Invalid params: expected object with 'id' field");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramsMap = (Map<String, Object>) params;

        Object idObj = paramsMap.get("id");
        if (idObj == null) {
            throw new IllegalArgumentException("Missing required parameter: id");
        }

        Long id = convertToLong(idObj);
        postService.delete(id);

        return Map.of("success", true, "message", "Post deleted successfully");
    }

    /**
     * Object를 Long으로 변환 (JSON에서 Number가 Integer 또는 Long으로 올 수 있음)
     */
    private Long convertToLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format: " + obj);
        }
    }
}

/**
 * JSON-RPC 전용 예외: 메서드를 찾을 수 없음
 */
class JsonRpcMethodNotFoundException extends RuntimeException {
    public JsonRpcMethodNotFoundException(String message) {
        super(message);
    }
}
