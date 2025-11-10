# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Doodle 폴더 개요

이 폴더는 **Java Best Practice 학습 및 실험용 코드**를 관리하는 공간입니다.

**목적**:
- 다양한 Java best practice 패턴을 독립적으로 실습
- 새로운 기술이나 라이브러리를 실험
- 작은 단위의 코드 예제와 PoC(Proof of Concept) 작성
- 리팩토링 전후 비교 코드 작성

**특징**:
- 각 하위 폴더는 독립적인 주제를 다룸
- 프로덕션 코드가 아닌 학습/실험용
- 빠른 프로토타이핑과 테스트 중심

## 폴더 구조

```
doodle/
├── CLAUDE.md                    # 이 파일
├── error-handling/              # Error as Value, Result 패턴
├── dto-patterns/                # DTO 변환 패턴, MapStruct 등
├── validation/                  # Bean Validation, 커스텀 검증
├── concurrency/                 # CompletableFuture, Virtual Threads
├── database-patterns/           # JPA 최적화, N+1 해결
├── testing/                     # 다양한 테스트 전략
├── reactive/                    # WebFlux, Reactor 패턴
├── api-design/                  # REST, GraphQL, gRPC 비교
└── performance/                 # 성능 최적화 기법
```

## 네이밍 컨벤션

### 폴더명
- **kebab-case** 사용 (예: `error-handling`, `dto-patterns`)
- 주제를 명확하게 표현하는 이름 사용
- 너무 길지 않게 (2-3 단어 이내 권장)

### 각 폴더 내부 구조
각 best practice 폴더는 다음과 같은 구조를 권장합니다:

```
topic-name/
├── README.md              # 해당 주제의 설명 및 실습 가이드
├── pom.xml 또는 build.gradle  # 빌드 설정 (필요시)
├── src/
│   ├── main/java/
│   │   ├── before/        # 리팩토링 전 코드 (선택)
│   │   └── after/         # 리팩토링 후 코드 (선택)
│   └── test/java/         # 테스트 코드
└── notes.md               # 학습 노트 (선택)
```

## 공통 개발 명령어

### Maven 프로젝트

```bash
# 폴더로 이동
cd doodle/error-handling

# 컴파일
mvn clean compile

# 테스트 실행
mvn test

# 실행 (Main 클래스가 있는 경우)
mvn exec:java -Dexec.mainClass="com.example.Main"

# 패키징
mvn clean package
```

### Gradle 프로젝트

```bash
# 폴더로 이동
cd doodle/dto-patterns

# 컴파일
./gradlew build

# 테스트 실행
./gradlew test

# 실행
./gradlew run

# 클린 빌드
./gradlew clean build
```

### 단일 Java 파일 실행

빌드 도구 없이 단일 파일로 실험하는 경우:

```bash
# 컴파일
javac MyExample.java

# 실행
java MyExample

# Java 11+ 단일 파일 실행 (컴파일 불필요)
java MyExample.java
```

## 코드 작성 가이드

### 1. 독립성 유지
- 각 폴더는 독립적으로 실행 가능해야 함
- 다른 폴더에 대한 의존성은 최소화
- 필요한 외부 라이브러리는 각 폴더의 `pom.xml` 또는 `build.gradle`에 명시

### 2. 문서화
- 각 폴더의 `README.md`에 다음 내용 포함:
  - **주제**: 무엇을 다루는가?
  - **학습 목표**: 어떤 best practice를 배우는가?
  - **실행 방법**: 어떻게 빌드하고 실행하는가?
  - **핵심 코드**: 주요 코드 스니펫과 설명
  - **참고 자료**: 관련 문서나 블로그 링크

### 3. 코드 스타일
- **Modern Java** 활용 (Records, Pattern Matching, Sealed Classes)
- **명시적 코드** 작성 (Golang 철학)
- **주석보다 자기문서화 코드** 우선
- 복잡한 부분에만 주석 추가

### 4. 테스트
- 가능한 한 테스트 코드 작성
- JUnit 5 사용
- 실습 중심이므로 100% 커버리지는 불필요

## Best Practice 주제 예시

### Error Handling (에러 처리)
- `Result<T, E>` 패턴 구현
- Either 모나드
- Exception vs Error as Value
- Custom Exception 계층 구조

### DTO Patterns (DTO 패턴)
- Entity to DTO 변환 전략
- MapStruct vs ModelMapper
- Record를 활용한 DTO
- DTO Validation

### Validation (검증)
- Bean Validation 활용
- 커스텀 Validator 작성
- 도메인 레벨 검증
- Cross-field validation

### Concurrency (동시성)
- CompletableFuture 패턴
- Virtual Threads (Java 21+)
- Reactive Streams
- Thread Pool 최적화

### Database Patterns (데이터베이스)
- N+1 문제 해결
- Batch Insert/Update
- Pessimistic vs Optimistic Locking
- Query DSL 활용

### Testing (테스트)
- Testcontainers 활용
- MockMvc vs WebTestClient
- Mockito 고급 패턴
- Architecture Testing (ArchUnit)

### API Design (API 설계)
- REST Best Practices
- GraphQL vs REST
- gRPC 서비스
- API Versioning 전략

### Performance (성능)
- 캐싱 전략 (Redis, Caffeine)
- 데이터베이스 인덱스 최적화
- 비동기 처리 패턴
- JVM 튜닝

## 사용 예시

### 새로운 주제 추가하기

```bash
# 1. 폴더 생성
mkdir -p doodle/my-topic/src/main/java/com/example
mkdir -p doodle/my-topic/src/test/java/com/example

# 2. README 작성
cat > doodle/my-topic/README.md << 'EOF'
# My Topic

## 주제
무엇을 다루는가?

## 학습 목표
- 목표 1
- 목표 2

## 실행 방법
\`\`\`bash
mvn test
\`\`\`

## 핵심 코드
...
EOF

# 3. 코드 작성
# src/main/java/com/example/Example.java

# 4. 테스트 작성
# src/test/java/com/example/ExampleTest.java
```

### 기존 주제 실습하기

```bash
# 1. 폴더로 이동
cd doodle/error-handling

# 2. README 읽기
cat README.md

# 3. 빌드 및 실행
mvn clean test

# 4. 코드 수정 및 실험
# src/main/java 파일 수정

# 5. 다시 테스트
mvn test
```

## Git 관리

### Commit 전략
- 각 주제별로 독립적으로 커밋
- 커밋 메시지 형식: `[doodle/topic-name] 설명`
  ```
  [doodle/error-handling] Add Result pattern implementation
  [doodle/dto-patterns] Add MapStruct example
  ```

### .gitignore
빌드 결과물은 Git에서 제외됩니다:
- `target/`, `build/` 디렉토리
- `*.class`, `*.jar` 파일
- IDE 설정 파일 (`.idea/`, `*.iml`)

소스 코드는 모두 Git으로 관리됩니다.

## 학습 팁

### 1. 작게 시작하기
- 한 번에 하나의 개념만 다루기
- 복잡한 예제보다 간단하고 명확한 예제

### 2. 비교 코드 작성
- `before/`와 `after/` 폴더로 리팩토링 전후 비교
- 왜 개선되었는지 README에 명시

### 3. 실제 문제에서 추출
- 실무에서 겪은 문제를 단순화하여 재현
- 해결 과정을 코드로 기록

### 4. 반복 학습
- 같은 주제를 다른 방식으로 여러 번 구현
- 각 구현의 장단점 비교

## 참고 자료

이 폴더는 다음 리소스와 연계되어 있습니다:
- [Best Practices](../best-practices/): 프로덕션 레벨 설계 패턴
- [Practical Guide](../practical-guide/): 현대적 Java 패턴과 안티패턴
- [Examples](../examples/): 실제 동작하는 프로젝트 예제

## 주의사항

1. **프로덕션 코드 아님**: 이 폴더의 코드는 학습/실험용이므로 그대로 프로덕션에 사용하지 말 것
2. **외부 의존성 최소화**: 핵심 개념 학습에 집중, 불필요한 라이브러리 추가 지양
3. **명확한 주제**: 각 폴더는 하나의 명확한 주제만 다룰 것
4. **README 필수**: 코드만 있고 설명이 없으면 나중에 이해하기 어려움

---

**Happy Learning! 🚀**
