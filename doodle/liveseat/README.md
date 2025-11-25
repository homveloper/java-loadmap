# LiveSeat - 공연 예매 시스템

**LiveSeat**은 Spring Boot와 Thymeleaf를 활용한 온라인 공연 예매 플랫폼입니다. Result 패턴, Optimistic Locking을 통한 동시성 제어, Package by Feature 구조 등 현대적인 Java/Spring 베스트 프랙티스를 적용한 학습용 프로젝트입니다.

## 📋 주요 기능

### 일반 사용자
- 공연 목록 조회 (페이징)
- 공연 상세 정보 확인
- 좌석 선택 및 예매 (동시성 제어)
- 내 예매 내역 조회 및 취소

### 관리자
- 공연 등록
- 예매 현황 대시보드
- 공연별 통계 조회

### 학습 포인트

이 프로젝트를 통해 다음을 학습할 수 있습니다:

1. **Result Pattern** - 예외 대신 `Result<T, E>`로 에러 처리
2. **Optimistic Locking** - `@Version`을 통한 동시성 제어
3. **Sealed Interface** - Java 21 패턴 매칭과 에러 타입 정의
4. **Package by Feature** - 도메인 중심 패키지 구조
5. **Thymeleaf SSR** - Server-Side Rendering
6. **Spring Data JPA** - Repository 패턴, 페이징, JPQL
7. **Bean Validation** - `@Valid`, `@NotBlank` 등
8. **REST API 설계** - Swagger/OpenAPI 문서화

## 🛠️ 기술 스택

- **Java 21**
- **Spring Boot 3.2.0**
- **Gradle 8.x**
- **Spring Data JPA** (Hibernate)
- **H2 Database** (in-memory)
- **Thymeleaf** (템플릿 엔진)
- **SpringDoc OpenAPI 3** (Swagger UI)
- **Bootstrap 5** (프론트엔드 UI)

## 🚀 빌드 및 실행

### 1. Gradle Wrapper 생성 (최초 1회)
```bash
gradle wrapper --gradle-version 8.5
```

### 2. 빌드
```bash
./gradlew clean build
```

### 3. 실행
```bash
./gradlew bootRun
```

### 4. 접속
- **웹 애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:liveseatdb`
  - Username: `sa`
  - Password: (비어있음)

## 📖 사용 가이드

### 일반 사용자 플로우

1. **공연 목록 조회** → http://localhost:8080/concerts
2. **공연 상세 보기** → 공연 카드 클릭
3. **예매하기** → 폼 작성 (이름, 이메일, 날짜, 좌석)
4. **내 예매 내역** → http://localhost:8080/bookings/my

### 관리자 플로우

1. **대시보드** → http://localhost:8080/admin
2. **공연 등록** → "새 공연 등록" 버튼
3. **통계 확인** → 공연별 "통계" 버튼

## 🔌 REST API 예제

### 공연 목록 조회
```bash
curl http://localhost:8080/api/concerts?page=0&size=10
```

### 예매 생성
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "concertId": 1,
    "customerName": "홍길동",
    "customerEmail": "hong@example.com",
    "bookingDate": "2026-02-15",
    "seatGrade": "VIP",
    "seatCount": 2
  }'
```

### 공연 등록 (관리자)
```bash
curl -X POST http://localhost:8080/api/admin/concerts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "뮤지컬 지킬 앤 하이드",
    "description": "선과 악의 이중 인격을 다룬 뮤지컬",
    "startDate": "2026-06-01",
    "endDate": "2026-08-31",
    "priceVip": 140000,
    "priceR": 100000,
    "priceS": 70000,
    "totalSeats": 250
  }'
```

## 💡 핵심 구현 포인트

### 1. Result Pattern 사용

```java
public Result<ConcertDetailResponse, ConcertError> getConcertById(Long id) {
    return concertRepository.findById(id)
        .map(ConcertDetailResponse::from)
        .<Result<ConcertDetailResponse, ConcertError>>map(Result::success)
        .orElse(Result.failure(new ConcertError.NotFound(id)));
}
```

### 2. Optimistic Locking으로 동시성 제어

```java
@Entity
public class Concert {
    @Version
    private Long version; // Optimistic Locking

    public boolean reserveSeats(int count) {
        if (availableSeats >= count) {
            availableSeats -= count;
            return true;
        }
        return false;
    }
}
```

### 3. Sealed Interface로 에러 타입 정의

```java
public sealed interface BookingError {
    record ConcertNotFound(Long concertId) implements BookingError {}
    record InvalidDate(LocalDate requested, LocalDate start, LocalDate end) implements BookingError {}
    record SoldOut(Long concertId, Integer requested, Integer available) implements BookingError {}
}
```

## 📚 추가 학습 주제

- [ ] Spring Security 추가
- [ ] JWT 인증 구현
- [ ] 파일 업로드 (공연 포스터)
- [ ] 이메일 발송
- [ ] 결제 API 연동
- [ ] WebSocket 실시간 업데이트
- [ ] Redis 캐싱
- [ ] 테스트 코드 작성

## 📄 라이선스

이 프로젝트는 학습 목적으로 자유롭게 사용 가능합니다.

---

**Happy Learning! 🚀**
