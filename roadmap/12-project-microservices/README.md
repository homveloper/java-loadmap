# 12. 실전 프로젝트 2: 마이크로서비스

> **프로젝트 기간**: 3-4주
> **난이도**: ⭐⭐⭐⭐⭐
> **전제 조건**: Phase 3 완료

## 🎯 프로젝트 목표

게임 서버를 모티브로 한 마이크로서비스 아키텍처를 설계하고 구현합니다.

## 📋 프로젝트 구성

### 서비스 아키텍처

```
┌─────────────┐
│   Gateway   │ (Spring Cloud Gateway)
└──────┬──────┘
       │
       ├──────────┬──────────┬──────────┬──────────┐
       │          │          │          │          │
   ┌───▼───┐  ┌──▼───┐  ┌───▼───┐  ┌──▼────┐  ┌──▼────┐
   │ User  │  │ Game │  │ Leader│  │ Notif │  │ Match │
   │Service│  │Service│  │ board │  │Service│  │ Making│
   └───┬───┘  └──┬───┘  └───┬───┘  └──┬────┘  └──┬────┘
       │         │          │         │          │
       └─────────┴──────────┴─────────┴──────────┘
                      │
              ┌───────▼────────┐
              │  Kafka/RabbitMQ │
              └────────────────┘
```

## 🛠 핵심 서비스

### 1. User Service
- 사용자 인증/인가
- JWT 토큰 발급
- 프로필 관리

### 2. Game Service
- 게임 세션 관리
- 게임 상태 저장
- 점수 계산

### 3. Leaderboard Service
- 실시간 랭킹
- Redis 캐싱
- 주기적 업데이트

### 4. Notification Service
- 이벤트 기반 알림
- WebSocket 실시간 알림
- 이메일/푸시 발송

### 5. Matchmaking Service
- 플레이어 매칭
- 큐 관리
- 밸런싱 알고리즘

## 🔧 기술 스택

- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Eureka Server
- **Config Server**: Spring Cloud Config
- **Circuit Breaker**: Resilience4j
- **Distributed Tracing**: Zipkin, Sleuth
- **Messaging**: Kafka
- **Cache**: Redis
- **Database**: MySQL (각 서비스 독립 DB)
- **Containerization**: Docker, Docker Compose
- **Orchestration**: Kubernetes

## 📖 참고 자료

- [Spring Cloud 공식 문서](https://spring.io/projects/spring-cloud)
- [마이크로서비스 패턴](https://microservices.io/patterns/index.html)

## ✅ 체크리스트

- [ ] API Gateway 구현
- [ ] Service Discovery 설정
- [ ] 각 서비스 구현
- [ ] 서비스 간 통신 (REST, gRPC, 메시징)
- [ ] Circuit Breaker 적용
- [ ] 분산 추적 설정
- [ ] Docker Compose 작성
- [ ] Kubernetes 배포
- [ ] 통합 테스트
- [ ] 문서화

## 🚀 다음 단계

**→ [13. 테스트 전략](../13-testing-strategy/)**
