# Blog API Example

README의 블로그 API 예제를 실제로 동작하는 단일 Java 파일로 구현한 프로젝트입니다.

## 프로젝트 구조

```
blog-api/
├── pom.xml                                          # Maven 빌드 설정
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/blog/
│       │       └── BlogApiApplication.java         # 모든 코드가 포함된 단일 파일
│       └── resources/
│           └── application.properties              # Spring Boot 설정
└── README.md
```

## 포함된 컴포넌트

`BlogApiApplication.java` 단일 파일에 다음 모든 컴포넌트가 포함되어 있습니다:

- **Spring Boot Application**: 메인 애플리케이션 클래스
- **Entity**: `Post` - JPA 엔티티
- **Repository**: `PostRepository` - Spring Data JPA 리포지토리
- **Service**: `PostService` - 비즈니스 로직
- **Controller**: `PostController` - REST API 엔드포인트
- **DTOs**: `CreatePostRequest`, `UpdatePostRequest` - 데이터 전송 객체
- **Exception Handler**: `GlobalExceptionHandler` - 전역 예외 처리
- **Custom Exceptions**: `ResourceNotFoundException` - 커스텀 예외

## 기술 스택

- Java 17
- Spring Boot 3.2.0
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- H2 Database (인메모리 데이터베이스)
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

- API 서버: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:blogdb`
  - Username: `sa`
  - Password: (비워두기)

## API 엔드포인트

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

## 학습 포인트

이 예제를 통해 다음을 학습할 수 있습니다:

1. **단일 파일 구조**: 모든 컴포넌트가 하나의 파일에 정리되어 있어 전체 구조를 한눈에 파악 가능
2. **REST API 설계**: RESTful 원칙에 따른 API 엔드포인트 설계
3. **Spring MVC**: `@RestController`, `@RequestMapping` 등의 어노테이션 활용
4. **Spring Data JPA**: JpaRepository를 통한 데이터베이스 CRUD 작업
5. **Bean Validation**: `@Valid`, `@NotBlank` 등을 통한 입력 검증
6. **예외 처리**: `@RestControllerAdvice`를 통한 전역 예외 처리
7. **DTO 패턴**: 요청/응답 데이터 분리

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
