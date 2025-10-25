# 15. CI/CD & 배포 전략

> **학습 기간**: 1주
> **난이도**: ⭐⭐⭐☆☆
> **전제 조건**: Docker, Git 기본

## 📚 학습 목표

자동화된 배포 파이프라인을 구축합니다.

## 🎯 핵심 내용

### 1. GitHub Actions

```yaml
# .github/workflows/ci.yml
name: CI/CD

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Build with Maven
      run: mvn clean package

    - name: Run tests
      run: mvn test

    - name: Build Docker image
      run: docker build -t myapp:${{ github.sha }} .

    - name: Push to Docker Hub
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push myapp:${{ github.sha }}
```

### 2. Dockerfile 최적화

```dockerfile
# Multi-stage build
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3. Docker Compose

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/mydb
    depends_on:
      - db
      - redis

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: mydb
    volumes:
      - db-data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  db-data:
```

### 4. Kubernetes 배포

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: myapp
        image: myapp:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5

---
apiVersion: v1
kind: Service
metadata:
  name: myapp-service
spec:
  selector:
    app: myapp
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

### 5. Helm Chart

```yaml
# Chart.yaml
apiVersion: v2
name: myapp
version: 1.0.0

# values.yaml
replicaCount: 3

image:
  repository: myapp
  tag: latest
  pullPolicy: IfNotPresent

service:
  type: LoadBalancer
  port: 80

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi
```

### 6. 무중단 배포 (Blue-Green)

```bash
# Blue 환경 배포
kubectl apply -f deployment-blue.yaml

# Green 환경 배포
kubectl apply -f deployment-green.yaml

# 트래픽 전환 (Service 업데이트)
kubectl patch service myapp -p '{"spec":{"selector":{"version":"green"}}}'

# Blue 환경 제거
kubectl delete deployment myapp-blue
```

### 7. 환경별 설정 관리

```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:testdb

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DATABASE_URL}
```

## 📖 학습 리소스

- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Kubernetes 공식 문서](https://kubernetes.io/docs/home/)
- [Helm 공식 문서](https://helm.sh/docs/)

## ✅ 체크리스트

- [ ] GitHub Actions 워크플로우
- [ ] Dockerfile 작성
- [ ] Docker Compose 설정
- [ ] Kubernetes Deployment
- [ ] Helm Chart
- [ ] 무중단 배포 전략
- [ ] 환경별 설정 관리

## 🎉 완료!

축하합니다! Java + Spring 학습 로드맵을 모두 완료했습니다.

이제 실무 프로젝트를 시작하고, 지속적으로 학습하며 경험을 쌓아가세요!

**다음 단계**:
1. 포트폴리오 프로젝트 완성
2. 오픈소스 기여
3. 기술 블로그 작성
4. 이직 준비 및 면접 연습
