# 07. Spring Data JPA & Database

> **학습 기간**: 1-2주
> **난이도**: ⭐⭐⭐⭐☆
> **전제 조건**: SQL 기본, Spring Boot

## 📚 학습 목표

JPA를 사용하여 데이터베이스와 효율적으로 연동합니다.

## 🎯 핵심 내용

### 1. Entity 정의
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String email;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### 2. Repository
```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Query Methods
    Optional<User> findByUsername(String username);
    List<User> findByEmailContaining(String email);

    // @Query
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmail(String email);

    // Native Query
    @Query(value = "SELECT * FROM users WHERE username = ?1", nativeQuery = true)
    User findByUsernameNative(String username);
}
```

### 3. 연관관계
```java
// 1:N
@Entity
public class Post {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;
}

@Entity
public class User {
    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();
}
```

### 4. @Transactional
```java
@Service
public class UserService {
    @Transactional
    public void transferPoints(Long fromId, Long toId, int points) {
        User from = userRepository.findById(fromId).orElseThrow();
        User to = userRepository.findById(toId).orElseThrow();

        from.decreasePoints(points);
        to.increasePoints(points);

        // 자동 커밋 (정상 종료 시)
        // 예외 발생 시 롤백
    }
}
```

### 5. N+1 문제 해결
```java
// 문제: N+1 쿼리 발생
List<Post> posts = postRepository.findAll();
posts.forEach(post -> System.out.println(post.getAuthor().getName())); // N번 쿼리

// 해결1: Fetch Join
@Query("SELECT p FROM Post p JOIN FETCH p.author")
List<Post> findAllWithAuthor();

// 해결2: @EntityGraph
@EntityGraph(attributePaths = {"author"})
List<Post> findAll();
```

## 📖 학습 리소스

- [김영한 - 자바 ORM 표준 JPA 프로그래밍](https://www.inflearn.com/course/ORM-JPA-Basic)
- [Spring Data JPA 공식 문서](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

## ✅ 체크리스트

- [ ] Entity 매핑
- [ ] Repository 작성
- [ ] Query Methods
- [ ] 연관관계 매핑
- [ ] @Transactional 이해
- [ ] N+1 문제 해결

## 🚀 다음 단계

**→ [08. Spring Security & 인증/인가](../08-spring-security/)**

## 💡 Golang 개발자를 위한 팁

- GORM과 유사하지만 더 강력한 기능
- 명시적 쿼리 vs ORM의 장단점 이해
