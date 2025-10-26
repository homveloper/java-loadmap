# Blog API Example

README의 블로그 API 예제를 실제로 동작하는 단일 Java 파일로 구현한 프로젝트입니다.

## 주요 기능

이 프로젝트는 동일한 블로그 API를 **네 가지 방식**으로 제공합니다:

1. **어노테이션 방식 REST API** (`@RestController`) - 전통적인 Spring MVC 방식
2. **Functional Endpoints** (`RouterFunction`) - 명시적 라우팅 방식 (Golang 스타일)
3. **JSON-RPC over HTTP** - 경량 RPC 프로토콜
4. **gRPC Service** - 고성능 바이너리 RPC 프로토콜

**모든 방식이 동일한 서비스 레이어(`PostService`)를 공유**하여, 하나의 비즈니스 로직을 여러 프로토콜로 노출하는 방법을 보여줍니다.

## 프로젝트 구조

```
blog-api/
├── pom.xml                                          # Maven 빌드 설정 (gRPC 포함)
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/blog/
│       │       └── BlogApiApplication.java         # 모든 코드가 포함된 단일 파일
│       ├── proto/
│       │   └── blog.proto                          # gRPC Protocol Buffer 정의
│       └── resources/
│           └── application.properties              # Spring Boot 설정
└── README.md
```

## 포함된 컴포넌트

`BlogApiApplication.java` 단일 파일에 다음 모든 컴포넌트가 포함되어 있습니다:

### 공통 컴포넌트
- **Spring Boot Application**: 메인 애플리케이션 클래스
- **Entity**: `Post` - JPA 엔티티
- **Repository**: `PostRepository` - Spring Data JPA 리포지토리
- **Service**: `PostService` - 비즈니스 로직
- **DTOs**: `CreatePostRequest`, `UpdatePostRequest` - 데이터 전송 객체
- **Exception Handler**: `GlobalExceptionHandler` - 전역 예외 처리
- **Custom Exceptions**: `ResourceNotFoundException` - 커스텀 예외

### API 레이어 (4가지 방식)
- **PostController** - 어노테이션 방식 REST API (`@RestController`)
- **PostHandler + PostRouter** - Functional Endpoints (명시적 라우팅)
- **JsonRpcController** - JSON-RPC over HTTP
- **BlogGrpcService** - gRPC 서비스 구현

## 기술 스택

- Java 17
- Spring Boot 3.2.0
- Spring Web MVC (어노테이션 + Functional Endpoints)
- Spring Data JPA
- Spring Validation
- H2 Database (인메모리 데이터베이스)
- gRPC + Protocol Buffers
- grpc-spring-boot-starter
- Maven

## 실행 방법

### 1. 필수 요구사항

- Java 17 이상
- Maven 3.6 이상

### 2. 빌드 및 실행

```bash
# 프로젝트 디렉토리로 이동
cd examples/blog-api

# Maven으로 빌드
mvn clean package

# 애플리케이션 실행
mvn spring-boot:run
```

또는 JAR 파일로 실행:

```bash
java -jar target/blog-api-1.0.0.jar
```

### 3. 애플리케이션 접속

- **REST API 서버**: http://localhost:8080
- **gRPC 서버**: localhost:9090
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:blogdb`
  - Username: `sa`
  - Password: (비워두기)

## API 엔드포인트

이 애플리케이션은 동일한 기능을 4가지 방식으로 제공합니다:

| 기능 | 어노테이션 방식 | Functional Endpoints | JSON-RPC | gRPC |
|------|----------------|---------------------|----------|------|
| 전체 조회 | GET /api/posts | GET /functional/posts | post.list | ListPosts |
| 단건 조회 | GET /api/posts/{id} | GET /functional/posts/{id} | post.get | GetPost |
| 생성 | POST /api/posts | POST /functional/posts | post.create | CreatePost |
| 수정 | PUT /api/posts/{id} | PUT /functional/posts/{id} | post.update | UpdatePost |
| 삭제 | DELETE /api/posts/{id} | DELETE /functional/posts/{id} | post.delete | DeletePost |

---

## A. 어노테이션 방식 REST API (`/api/posts`)

전통적인 Spring MVC 방식으로, `@RestController` 어노테이션을 사용합니다.

### 1. 모든 게시글 조회

```bash
GET /api/posts

# 예시
curl http://localhost:8080/api/posts
```

**응답 예시**:
```json
[
  {
    "id": 1,
    "title": "첫 번째 게시글",
    "content": "게시글 내용입니다.",
    "author": "홍길동",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": null
  }
]
```

### 2. 특정 게시글 조회

```bash
GET /api/posts/{id}

# 예시
curl http://localhost:8080/api/posts/1
```

### 3. 새 게시글 생성

```bash
POST /api/posts
Content-Type: application/json

# 예시
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "새 게시글",
    "content": "게시글 내용입니다.",
    "author": "홍길동"
  }'
```

**응답**: 201 Created + Location 헤더

### 4. 게시글 수정

```bash
PUT /api/posts/{id}
Content-Type: application/json

# 예시
curl -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "수정된 제목",
    "content": "수정된 내용"
  }'
```

### 5. 게시글 삭제

```bash
DELETE /api/posts/{id}

# 예시
curl -X DELETE http://localhost:8080/api/posts/1
```

**응답**: 204 No Content

### 6. 작성자별 게시글 조회

```bash
GET /api/posts?author={author}

# 예시
curl "http://localhost:8080/api/posts?author=홍길동"
```

## 테스트 시나리오

### 시나리오 1: 게시글 CRUD

```bash
# 1. 게시글 생성
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot 학습",
    "content": "Spring Boot는 Spring 기반 애플리케이션을 쉽게 만들 수 있게 해줍니다.",
    "author": "김개발"
  }'

# 2. 모든 게시글 조회
curl http://localhost:8080/api/posts

# 3. 특정 게시글 조회
curl http://localhost:8080/api/posts/1

# 4. 게시글 수정
curl -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot 완벽 가이드",
    "content": "Spring Boot는 정말 강력한 프레임워크입니다!"
  }'

# 5. 게시글 삭제
curl -X DELETE http://localhost:8080/api/posts/1
```

### 시나리오 2: 여러 게시글 생성 및 조회

```bash
# 게시글 3개 생성
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title": "Java 기초", "content": "Java 학습 시작", "author": "김자바"}'

curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title": "Spring 입문", "content": "Spring 학습 중", "author": "이스프링"}'

curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title": "JPA 활용", "content": "JPA로 데이터베이스 연동", "author": "박디비"}'

# 모든 게시글 조회
curl http://localhost:8080/api/posts

# 특정 작성자의 게시글만 조회
curl "http://localhost:8080/api/posts?author=김자바"
```

## 주요 기능

### 1. 데이터 검증 (Bean Validation)

CreatePostRequest와 UpdatePostRequest에는 검증 어노테이션이 적용되어 있습니다:

- `@NotBlank`: 필수 필드 검증
- `@Size`: 문자열 길이 검증

잘못된 요청 예시:

```bash
# 제목이 비어있는 경우
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title": "", "content": "내용", "author": "작성자"}'
```

**응답**: 400 Bad Request
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 2. 예외 처리

GlobalExceptionHandler가 다음 예외들을 처리합니다:

- `ResourceNotFoundException`: 404 Not Found
- `MethodArgumentNotValidException`: 400 Bad Request (검증 실패)
- `Exception`: 500 Internal Server Error

### 3. 자동 타임스탬프

Post 엔티티는 `@PrePersist`와 `@PreUpdate`를 사용하여 자동으로 생성 및 수정 시간을 기록합니다.

---

## B. Functional Endpoints (`/functional/posts`)

명시적 라우팅 방식으로, Golang의 Gin/Echo와 유사한 패턴입니다.

**특징**:
- `RouterFunction`과 `HandlerFunction`을 사용하여 명시적으로 라우트 정의
- 어노테이션 없이 프로그래매틱하게 라우팅 설정
- 필터와 미들웨어를 체이닝 방식으로 적용 가능

### 예시

```bash
# 모든 게시글 조회
curl http://localhost:8080/functional/posts

# 특정 게시글 조회
curl http://localhost:8080/functional/posts/1

# 게시글 생성
curl -X POST http://localhost:8080/functional/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"새 게시글","content":"내용","author":"작성자"}'

# 게시글 수정
curl -X PUT http://localhost:8080/functional/posts/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"수정된 제목","content":"수정된 내용"}'

# 게시글 삭제
curl -X DELETE http://localhost:8080/functional/posts/1
```

**코드 비교**:

```java
// 어노테이션 방식
@RestController
@RequestMapping("/api/posts")
public class PostController {
    @GetMapping("/{id}")
    public Post getPost(@PathVariable Long id) { ... }
}

// Functional Endpoints 방식
@Configuration
public class PostRouter {
    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler handler) {
        return route(GET("/functional/posts/{id}"), handler::getPost);
    }
}
```

---

## C. JSON-RPC over HTTP (`POST /jsonrpc`)

JSON-RPC 2.0 프로토콜을 사용하는 경량 RPC 방식입니다.

**특징**:
- 단일 엔드포인트로 모든 메서드 호출
- JSON 형식의 요청/응답 (REST와 유사하지만 메서드 중심)
- 표준화된 에러 코드
- REST보다 간단한 API 설계

### JSON-RPC 2.0 프로토콜 구조

**요청 형식**:
```json
{
  "jsonrpc": "2.0",
  "method": "methodName",
  "params": {...},
  "id": 1
}
```

**성공 응답**:
```json
{
  "jsonrpc": "2.0",
  "result": {...},
  "id": 1
}
```

**에러 응답**:
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": -32600,
    "message": "Invalid Request"
  },
  "id": 1
}
```

### 사용 예시

```bash
# 모든 게시글 조회
curl -X POST http://localhost:8080/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "post.list",
    "params": {},
    "id": 1
  }'

# 작성자별 필터링
curl -X POST http://localhost:8080/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "post.list",
    "params": {"author": "홍길동"},
    "id": 1
  }'

# 특정 게시글 조회
curl -X POST http://localhost:8080/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "post.get",
    "params": {"id": 1},
    "id": 1
  }'

# 게시글 생성
curl -X POST http://localhost:8080/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "post.create",
    "params": {
      "title": "JSON-RPC로 생성한 게시글",
      "content": "JSON-RPC는 간단하고 효과적입니다!",
      "author": "김개발"
    },
    "id": 1
  }'

# 게시글 수정
curl -X POST http://localhost:8080/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "post.update",
    "params": {
      "id": 1,
      "title": "수정된 제목",
      "content": "수정된 내용"
    },
    "id": 1
  }'

# 게시글 삭제
curl -X POST http://localhost:8080/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "post.delete",
    "params": {"id": 1},
    "id": 1
  }'
```

### JSON-RPC 에러 코드

| 코드 | 의미 | 설명 |
|------|------|------|
| -32700 | Parse error | JSON 파싱 실패 |
| -32600 | Invalid Request | 잘못된 요청 형식 |
| -32601 | Method not found | 존재하지 않는 메서드 |
| -32602 | Invalid params | 잘못된 파라미터 |
| -32603 | Internal error | 내부 서버 에러 |
| -32001 | Resource not found | 리소스 없음 (커스텀) |
| -32002 | Validation error | 검증 실패 (커스텀) |

### REST vs JSON-RPC 비교

| 특성 | REST | JSON-RPC |
|------|------|----------|
| 엔드포인트 | 리소스별 다수 | 단일 (/jsonrpc) |
| HTTP 메서드 | GET, POST, PUT, DELETE | POST만 사용 |
| 개념 | 리소스 중심 | 메서드/액션 중심 |
| 캐싱 | HTTP 캐싱 가능 | 불가능 (모두 POST) |
| 에러 처리 | HTTP 상태 코드 | JSON-RPC 에러 코드 |
| 복잡도 | 중간 | 낮음 |
| 사용 사례 | 공개 API, 웹 서비스 | 내부 API, 간단한 RPC |

---

## D. gRPC API (`:9090`)

고성능 RPC 프로토콜로, HTTP/2와 Protocol Buffers를 사용합니다.

**특징**:
- 바이너리 프로토콜로 JSON보다 빠르고 효율적
- 타입 안전성 (proto 파일로 스키마 정의)
- 양방향 스트리밍 지원
- 다국어 클라이언트 자동 생성

### 사용 방법

#### 1. grpcurl 설치

```bash
# Mac
brew install grpcurl

# Linux
wget https://github.com/fullstorydev/grpcurl/releases/download/v1.8.9/grpcurl_1.8.9_linux_x86_64.tar.gz
tar -xvf grpcurl_1.8.9_linux_x86_64.tar.gz
sudo mv grpcurl /usr/local/bin/

# Windows (Chocolatey)
choco install grpcurl
```

#### 2. gRPC 서비스 확인

```bash
# 서비스 목록 확인
grpcurl -plaintext localhost:9090 list

# 서비스 메서드 확인
grpcurl -plaintext localhost:9090 list blog.BlogService

# 메서드 상세 정보
grpcurl -plaintext localhost:9090 describe blog.BlogService.CreatePost
```

#### 3. gRPC 호출 예시

```bash
# 모든 게시글 조회
grpcurl -plaintext -d '{}' localhost:9090 blog.BlogService/ListPosts

# 작성자별 필터링
grpcurl -plaintext -d '{"author":"홍길동"}' localhost:9090 blog.BlogService/ListPosts

# 특정 게시글 조회
grpcurl -plaintext -d '{"id":1}' localhost:9090 blog.BlogService/GetPost

# 게시글 생성
grpcurl -plaintext -d '{
  "title":"gRPC로 생성한 게시글",
  "content":"gRPC는 빠르고 효율적입니다!",
  "author":"김개발"
}' localhost:9090 blog.BlogService/CreatePost

# 게시글 수정
grpcurl -plaintext -d '{
  "id":1,
  "title":"수정된 제목",
  "content":"수정된 내용"
}' localhost:9090 blog.BlogService/UpdatePost

# 게시글 삭제
grpcurl -plaintext -d '{"id":1}' localhost:9090 blog.BlogService/DeletePost
```

### 4가지 API 스타일 비교표

| 특성 | REST | Functional Endpoints | JSON-RPC | gRPC |
|------|------|---------------------|----------|------|
| **프로토콜** | HTTP/1.1 | HTTP/1.1 | HTTP/1.1 | HTTP/2 |
| **데이터 형식** | JSON | JSON | JSON | Protobuf (바이너리) |
| **엔드포인트** | 리소스별 다수 | 리소스별 다수 | 단일 | 서비스별 |
| **개념** | 리소스 중심 | 리소스 중심 | 메서드 중심 | 메서드 중심 |
| **HTTP 메서드** | GET, POST, PUT, DELETE | GET, POST, PUT, DELETE | POST만 | POST만 |
| **스키마** | 선택적 (OpenAPI) | 선택적 | 선택적 | 필수 (.proto) |
| **성능** | 보통 | 보통 | 보통 | 빠름 (3-10배) |
| **브라우저 지원** | 좋음 | 좋음 | 좋음 | 제한적 |
| **캐싱** | 가능 | 가능 | 불가능 | 불가능 |
| **스트리밍** | 제한적 | 제한적 | 없음 | 양방향 지원 |
| **코드 스타일** | 어노테이션 | 함수형/명시적 | 메서드 라우팅 | Proto 정의 |
| **복잡도** | 중간 | 중간 | 낮음 | 높음 |
| **사용 사례** | 공개 API | 동적 라우팅 | 간단한 내부 RPC | 마이크로서비스 |

### 언제 어떤 방식을 사용할까?

- **REST (어노테이션)**: 공개 API, 표준적인 웹 서비스, 대부분의 경우
- **Functional Endpoints**: 동적 라우팅 필요, WebFlux 사용, 함수형 프로그래밍 선호
- **JSON-RPC**: 간단한 내부 API, 메서드 기반 통신, 최소한의 복잡도
- **gRPC**: 마이크로서비스 간 통신, 고성능 필요, 양방향 스트리밍

---

## 학습 포인트

이 예제를 통해 다음을 학습할 수 있습니다:

### 1. 다양한 API 스타일 (동일한 서비스 레이어 공유)
- **어노테이션 방식**: 간결하고 직관적, 대부분의 Spring 프로젝트에서 사용
- **Functional Endpoints**: 명시적 라우팅, 동적 설정 가능, WebFlux와 호환성
- **JSON-RPC**: 경량 RPC, 단일 엔드포인트, 메서드 기반 통신
- **gRPC**: 고성능 바이너리 RPC, 마이크로서비스 아키텍처에 적합

### 2. Spring 핵심 개념
- **단일 파일 구조**: 모든 컴포넌트가 하나의 파일에 정리되어 있어 전체 구조를 한눈에 파악 가능
- **REST API 설계**: RESTful 원칙에 따른 API 엔드포인트 설계
- **Spring MVC**: `@RestController`, `@RequestMapping` 등의 어노테이션 활용
- **Spring Data JPA**: JpaRepository를 통한 데이터베이스 CRUD 작업
- **Bean Validation**: `@Valid`, `@NotBlank` 등을 통한 입력 검증
- **예외 처리**: `@RestControllerAdvice`를 통한 전역 예외 처리
- **DTO 패턴**: 요청/응답 데이터 분리

### 3. 실전 패턴
- **다중 프로토콜 지원**: 하나의 비즈니스 로직을 여러 프로토콜로 노출
- **Golang 스타일 라우팅**: Functional Endpoints로 Gin/Echo와 유사한 패턴 구현
- **RPC 통신**: JSON-RPC와 gRPC를 통한 효율적인 서비스 간 통신
- **프로토콜 버퍼**: 타입 안전한 API 계약
- **서비스 레이어 재사용**: 모든 컨트롤러가 동일한 `PostService` 사용

## 확장 아이디어

이 프로젝트를 확장하여 다음 기능들을 추가해볼 수 있습니다:

- [ ] 페이징 및 정렬 기능
- [ ] 검색 기능 (제목, 내용으로 검색)
- [ ] 댓글 기능 추가
- [ ] 카테고리/태그 기능
- [ ] 파일 업로드 기능
- [ ] JWT 인증/인가
- [ ] Swagger/OpenAPI 문서화
- [ ] 단위 테스트 및 통합 테스트
- [ ] PostgreSQL/MySQL 등 실제 데이터베이스 연동

## 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Web MVC 가이드](../../roadmap/06-spring-web-mvc/)
- [Spring Data JPA 가이드](../../roadmap/07-spring-data-jpa/)
