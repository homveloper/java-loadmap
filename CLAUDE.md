# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is a **Java + Spring Boot learning roadmap repository** designed for backend developers transitioning from Golang/C++ to Java. The repository contains:

- **Structured learning materials** in Korean (한글) organized across 15 phases
- **Practical guides** focusing on modern Java patterns and anti-patterns
- **Working code examples** including a complete Blog API implementation
- **Best practices** combining Golang's pragmatism with Java's type safety

**Target Audience**: 3-5 year mid-level developers transitioning to Java from Golang/C++ backgrounds, particularly those with game server experience.

## Project Structure

```
java-loadmap/
├── roadmap/              # 15-phase learning roadmap (Phase 1-4)
│   ├── 01-java-basics/          # Java fundamentals
│   ├── 02-jvm-memory/           # JVM & memory management
│   ├── 03-java-concurrency/     # Concurrency & multithreading
│   ├── 04-spring-core/          # Spring Core & DI
│   ├── 05-spring-boot/          # Spring Boot basics
│   ├── 06-spring-web-mvc/       # REST API development
│   ├── 07-spring-data-jpa/      # Database & JPA
│   ├── 08-spring-security/      # Security & authentication
│   ├── 09-project-rest-api/     # Practical project 1
│   ├── 10-spring-webflux/       # Reactive programming
│   ├── 11-messaging-events/     # Messaging & event architecture
│   ├── 12-project-microservices/# Practical project 2
│   ├── 13-testing-strategy/     # Testing strategies
│   ├── 14-performance-monitoring/ # Performance & monitoring
│   └── 15-cicd-deployment/      # CI/CD & deployment
│
├── practical-guide/      # Modern Java patterns & anti-patterns
│   ├── 01-anti-patterns.md      # Common pitfalls to avoid
│   ├── 02-modern-java.md        # Java 8-21 features
│   ├── 03-code-structure.md     # Code organization
│   ├── 04-collaboration.md      # Team practices
│   └── examples/                # Before/After refactoring examples
│
├── best-practices/       # Production-level design patterns
│   ├── README.md                # Golang + Java philosophy fusion
│   └── examples/                # Result pattern, Value Objects, etc.
│
└── examples/
    └── blog-api/         # Working Blog API (single-file implementation)
        ├── README.md     # Full documentation with curl examples
        ├── pom.xml       # Maven build configuration
        └── src/main/java/.../BlogApiApplication.java  # All-in-one implementation
```

## Philosophy & Design Principles

This repository emphasizes a **pragmatic, Golang-inspired approach** to Java development:

### Core Principles

1. **Error as Value** - Use `Result<T, E>` pattern instead of throwing exceptions for predictable business errors
2. **Composition over Inheritance** - Prefer composition for flexibility
3. **Simplicity over Complexity** - Avoid over-engineering and unnecessary abstraction
4. **Explicit over Implicit** - Make control flow and dependencies clear
5. **Modern Java Features** - Leverage Records, Pattern Matching, Sealed Classes (Java 14+)

### Key Patterns

- **Result Pattern**: See `best-practices/examples/Result.java` for complete implementation
- **Value Objects**: Avoid primitive obsession, encapsulate domain concepts
- **Static Factory Methods**: Use meaningful constructors over bare `new` keyword
- **Rich Domain Models**: Business logic belongs in domain objects, not anemic entities
- **Functional Endpoints**: Alternative to `@RestController` for Golang-style routing

## Common Development Commands

### Blog API Example

The working example is in `examples/blog-api/` - a single-file Spring Boot application demonstrating **four API styles**:

1. **REST API** (traditional `@RestController`)
2. **Functional Endpoints** (Golang Gin/Echo-style routing)
3. **JSON-RPC** over HTTP
4. **gRPC** (high-performance binary protocol)

```bash
# Navigate to example
cd examples/blog-api

# Build and run
mvn clean package
mvn spring-boot:run

# Run JAR directly
java -jar target/blog-api-1.0.0.jar

# Access endpoints
# - REST API: http://localhost:8080/api/posts
# - Functional: http://localhost:8080/functional/posts
# - JSON-RPC: http://localhost:8080/jsonrpc (POST)
# - gRPC: localhost:9090
# - H2 Console: http://localhost:8080/h2-console

# Test REST API
curl http://localhost:8080/api/posts
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Post","content":"Content","author":"Author"}'

# Test gRPC (requires grpcurl)
grpcurl -plaintext -d '{}' localhost:9090 blog.BlogService/ListPosts
```

### Presentation Generation

This repo includes Node.js tooling for creating PowerPoint presentations:

```bash
# Generate roadmap presentation
npm install
node create-presentation.js
```

## Architecture Notes

### Blog API Implementation

- **Single-file architecture**: All components in `BlogApiApplication.java` for learning clarity
- **Shared service layer**: `PostService` is reused across all four API styles
- **Entity design**: Uses JPA annotations with auto-timestamps (`@PrePersist`, `@PreUpdate`)
- **Validation**: Bean Validation with `@Valid`, `@NotBlank`, `@Size`
- **Exception handling**: `@RestControllerAdvice` with `GlobalExceptionHandler`
- **H2 in-memory database**: No external DB required for quick testing

### Result Pattern Usage

When working with the Result pattern (found in `best-practices/examples/Result.java`):

```java
// Define error types as sealed interfaces or enums
sealed interface UserError permits UserNotFound, UserInactive { }
record UserNotFound(Long id) implements UserError { }
record UserInactive(Long id) implements UserError { }

// Service methods return Result instead of throwing
public Result<User, UserError> getUserById(Long id) {
    return userRepository.findById(id)
        .map(user -> user.isActive()
            ? Result.<User, UserError>success(user)
            : Result.<User, UserError>failure(new UserInactive(id)))
        .orElse(Result.failure(new UserNotFound(id)));
}

// Controllers handle both cases explicitly
return switch (result) {
    case Result.Success<User, UserError> s -> ResponseEntity.ok(s.value());
    case Result.Failure<User, UserError> f -> switch (f.error()) {
        case UserNotFound e -> ResponseEntity.notFound().build();
        case UserInactive e -> ResponseEntity.status(403).build();
    };
};
```

## Language & Documentation

- **Primary language**: Korean (한글)
- **Code comments**: Mix of Korean and English
- **Variable/method names**: English (following Java conventions)
- When creating documentation, maintain Korean for explanatory text but use English for code

## Important Conventions

### File Naming
- Markdown files: Use lowercase with hyphens (`01-anti-patterns.md`)
- Java files: PascalCase (`BlogApiApplication.java`, `Result.java`)
- Directories: lowercase with hyphens

### Code Style
- **Prefer Records** over traditional POJOs when possible (Java 14+)
- **Use sealed interfaces** for exhaustive pattern matching (Java 17+)
- **Leverage pattern matching** in switch expressions (Java 21+)
- **Avoid getter/setter anti-pattern** - encapsulate behavior in domain objects
- **Minimize null usage** - prefer Optional or Result types

### Documentation Requirements
Each roadmap phase (in `roadmap/`) should include:
- Learning objectives (목표)
- Difficulty rating (난이도: ⭐-⭐⭐⭐⭐⭐)
- Time estimate (예상 기간)
- Prerequisites (전제 조건)
- Comparisons with Golang/C++ (비교 포인트)
- Practical examples
- Checklist (✅ 체크리스트)

## Testing Notes

The repository focuses on practical examples over comprehensive test coverage. When adding tests:

- Use **JUnit 5** (not JUnit 4)
- Leverage **@SpringBootTest** for integration tests
- Use **MockMvc** for controller testing
- Consider **Testcontainers** for database integration tests
- Examples should be in `src/test/java` (not currently present in blog-api)

## Common Gotchas

1. **Don't over-engineer**: This repository explicitly teaches against excessive abstraction
2. **Result vs Optional**: Use Result for business errors, Optional only for "value may not exist"
3. **Exception usage**: Reserve exceptions for truly exceptional cases (I/O errors, system failures)
4. **JPA N+1 problem**: Always consider fetch strategies (covered in roadmap/07-spring-data-jpa/)
5. **Transaction boundaries**: `@Transactional` belongs on service layer, not controllers

## Build Configuration

When working with Maven projects in this repo:
- Java version: **17** (or 21 for latest features)
- Spring Boot version: **3.x** (examples use 3.2.0)
- Key dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation
- For gRPC examples: grpc-spring-boot-starter, protobuf-maven-plugin

## Related Resources

The README.md references several external learning resources:
- 백기선의 스프링 부트 (Korean Spring Boot course)
- 김영한의 스프링 완전 정복 로드맵 (Korean Spring mastery roadmap)
- Effective Java (Joshua Bloch) - recommended reading
- Baeldung tutorials - practical Java examples

When suggesting additional learning materials, prioritize Korean-language resources for consistency with the target audience.
