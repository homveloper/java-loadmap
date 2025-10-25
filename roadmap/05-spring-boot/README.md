# 05. Spring Boot 기초

> **학습 기간**: 1-2주
> **난이도**: ⭐⭐☆☆☆
> **전제 조건**: Spring Core 이해

## 📚 학습 목표

Spring Boot의 자동 설정과 편의 기능을 활용하여 빠르게 애플리케이션을 개발합니다.

## 🎯 핵심 내용

### 1. Spring Boot 특징
- **Auto Configuration**: 자동 설정
- **Starter Dependencies**: 의존성 간소화
- **Embedded Server**: Tomcat/Jetty 내장
- **Production Ready**: Actuator

### 2. 프로젝트 생성
- [Spring Initializr](https://start.spring.io/)
- IntelliJ IDEA New Project

### 3. application.yml 설정
```yaml
server:
  port: 8080

spring:
  application:
    name: my-app
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 4. 프로파일 (dev, prod)
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb

# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-server:3306/proddb
```

```bash
# 프로파일 활성화
java -jar myapp.jar --spring.profiles.active=prod
```

### 5. Actuator
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

접근: `http://localhost:8080/actuator/health`

### 6. 빌드 도구

**Maven (pom.xml)**:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

**Gradle (build.gradle)**:
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

## 📖 학습 리소스

- [Spring Boot 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [백기선 - 스프링 부트 개념과 활용](https://www.inflearn.com/course/스프링부트)

## ✅ 체크리스트

- [ ] Spring Initializr로 프로젝트 생성
- [ ] application.yml 설정
- [ ] 프로파일 설정 (dev, prod)
- [ ] Actuator 활성화
- [ ] Maven/Gradle 이해

## 🚀 다음 단계

**→ [06. Spring Web MVC](../06-spring-web-mvc/)**
