# 14. 성능 최적화 & 모니터링

> **학습 기간**: 1-2주
> **난이도**: ⭐⭐⭐⭐☆
> **전제 조건**: JVM 이해, Spring Boot

## 📚 학습 목표

게임 서버 경험을 활용하여 프로덕션 레벨의 성능을 달성합니다.

## 🎯 핵심 내용

### 1. JVM 튜닝

```bash
# 힙 크기 설정
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar myapp.jar

# GC 로그
java -Xlog:gc*:file=gc.log \
     -jar myapp.jar
```

### 2. 데이터베이스 쿼리 최적화

```java
// N+1 문제 해결
@EntityGraph(attributePaths = {"author", "comments"})
List<Post> findAll();

// Batch Fetch
@BatchSize(size = 10)
@OneToMany(mappedBy = "post")
private List<Comment> comments;

// 쿼리 힌트
@QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
List<Post> findAllReadOnly();
```

### 3. 캐싱 전략

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultCacheConfig())
            .build();
    }
}

@Service
public class UserService {

    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @CacheEvict(value = "users", key = "#user.id")
    public User update(User user) {
        return userRepository.save(user);
    }
}
```

### 4. Connection Pool 튜닝

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 5. 로깅 전략

```java
// 구조화된 로깅
@Slf4j
@RestController
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Getting user with id: {}", id);
        User user = userService.findById(id);
        log.debug("User details: {}", user);
        return user;
    }
}
```

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <logger name="com.example" level="DEBUG"/>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### 6. 메트릭 수집 (Micrometer + Prometheus)

```java
@Configuration
public class MetricsConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

@Service
public class UserService {

    @Timed(value = "user.service.findById", description = "Time to find user by ID")
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

```yaml
# application.yml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 7. APM (Application Performance Monitoring)

- **New Relic**: 실시간 모니터링
- **Datadog**: 메트릭, 로그, 트레이스
- **Elastic APM**: Elasticsearch 기반

## 📖 학습 리소스

- [Java Performance: The Definitive Guide](https://www.oreilly.com/library/view/java-performance-the/9781449363512/)
- [High Performance MySQL](https://www.oreilly.com/library/view/high-performance-mysql/9781492080503/)

## ✅ 체크리스트

- [ ] JVM 옵션 튜닝
- [ ] 쿼리 최적화
- [ ] Redis 캐싱 구현
- [ ] Connection Pool 설정
- [ ] 구조화된 로깅
- [ ] Prometheus 메트릭 수집
- [ ] Grafana 대시보드 구성

## 🚀 다음 단계

**→ [15. CI/CD & 배포 전략](../15-cicd-deployment/)**

## 💡 게임 서버 경험 활용

- 낮은 레이턴시 요구사항
- 높은 동시성 처리
- 실시간 모니터링 중요성
