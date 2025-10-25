# 11. 메시징 & 이벤트 기반 아키텍처

> **학습 기간**: 1-2주
> **난이도**: ⭐⭐⭐⭐☆
> **전제 조건**: Spring Boot, 비동기 프로그래밍 이해

## 📚 학습 목표

Kafka/RabbitMQ를 사용하여 이벤트 기반 아키텍처를 구축합니다.

## 🎯 핵심 내용

### 1. Spring Kafka

```java
// Producer
@Service
public class EventProducer {

    @Autowired
    private KafkaTemplate<String, Event> kafkaTemplate;

    public void sendEvent(Event event) {
        kafkaTemplate.send("user-events", event.getId(), event);
    }
}

// Consumer
@Service
public class EventConsumer {

    @KafkaListener(topics = "user-events", groupId = "user-service")
    public void handleEvent(Event event) {
        System.out.println("Received event: " + event);
        // 이벤트 처리 로직
    }
}
```

### 2. 이벤트 기반 아키텍처

```java
// 도메인 이벤트
public record UserRegisteredEvent(String userId, String email, LocalDateTime timestamp) {}

// 이벤트 발행
@Service
public class UserService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public User register(RegisterRequest request) {
        User user = userRepository.save(new User(request));

        // 이벤트 발행
        eventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(),
            user.getEmail(),
            LocalDateTime.now()
        ));

        return user;
    }
}

// 이벤트 리스너
@Component
public class UserEventListener {

    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        // 환영 이메일 발송
        emailService.sendWelcomeEmail(event.email());

        // 통계 업데이트
        analyticsService.recordUserRegistration(event);
    }
}
```

### 3. Outbox 패턴

```java
// 트랜잭션 안전한 이벤트 발행
@Service
public class UserService {

    @Transactional
    public User register(RegisterRequest request) {
        User user = userRepository.save(new User(request));

        // Outbox 테이블에 이벤트 저장
        OutboxEvent outboxEvent = new OutboxEvent(
            "UserRegistered",
            objectMapper.writeValueAsString(user)
        );
        outboxRepository.save(outboxEvent);

        return user;
    }
}

// 백그라운드에서 Outbox 이벤트를 Kafka로 전송
@Scheduled(fixedDelay = 1000)
public void publishOutboxEvents() {
    List<OutboxEvent> events = outboxRepository.findUnpublished();
    events.forEach(event -> {
        kafkaTemplate.send(event.getTopic(), event.getPayload());
        event.setPublished(true);
        outboxRepository.save(event);
    });
}
```

## 📖 학습 리소스

- [Spring Kafka 공식 문서](https://docs.spring.io/spring-kafka/reference/html/)
- [Event-Driven Architecture 패턴](https://martinfowler.com/articles/201701-event-driven.html)

## ✅ 체크리스트

- [ ] Kafka Producer/Consumer
- [ ] 이벤트 발행/구독
- [ ] Dead Letter Queue
- [ ] Outbox 패턴

## 🚀 다음 단계

**→ [12. 실전 프로젝트 2: 마이크로서비스](../12-project-microservices/)**
