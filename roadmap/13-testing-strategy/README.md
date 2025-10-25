# 13. 테스트 전략

> **학습 기간**: 1주
> **난이도**: ⭐⭐⭐☆☆
> **전제 조건**: JUnit 기본

## 📚 학습 목표

종합적인 테스트 전략을 수립하고 자동화합니다.

## 🎯 핵심 내용

### 1. 단위 테스트 (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_Success() {
        // Given
        User user = new User(1L, "john", "john@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        User result = userService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findById(1L);
        });
    }
}
```

### 2. 통합 테스트 (@SpringBootTest)

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest("john", "john@example.com");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("john"));
    }
}
```

### 3. Testcontainers (데이터베이스 테스트)

```java
@SpringBootTest
@Testcontainers
class UserRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFind() {
        User user = new User("john", "john@example.com");
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
    }
}
```

### 4. API 테스트 (RestAssured)

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void getUsers_Success() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }
}
```

### 5. 테스트 커버리지 (JaCoCo)

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 📖 학습 리소스

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Testcontainers 공식 문서](https://www.testcontainers.org/)

## ✅ 체크리스트

- [ ] JUnit 5 단위 테스트
- [ ] Mockito Mock 테스트
- [ ] @SpringBootTest 통합 테스트
- [ ] Testcontainers
- [ ] RestAssured API 테스트
- [ ] 테스트 커버리지 80% 이상

## 🚀 다음 단계

**→ [14. 성능 최적화 & 모니터링](../14-performance-monitoring/)**
