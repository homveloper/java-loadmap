const PptxGenJS = require('pptxgenjs');
const pptx = new PptxGenJS();

// 16:9 슬라이드 레이아웃 설정
pptx.layout = 'LAYOUT_16x9';
pptx.author = 'Java Roadmap';
pptx.title = 'Java & Spring 완벽 학습 로드맵';

// 색상 팔레트 (Classic Blue)
const colors = {
  primary: '1C2833',
  secondary: '2E4053',
  accent: '3498DB',
  light: 'F4F6F6',
  white: 'FFFFFF',
  text: '2C3E50'
};

// 타이틀 슬라이드
let slide = pptx.addSlide();
slide.background = { color: colors.primary };
slide.addText('Java & Spring', {
  x: 0.5, y: 2.0, w: 9, h: 1.5,
  fontSize: 60, bold: true, color: colors.white, align: 'center'
});
slide.addText('완벽 학습 로드맵', {
  x: 0.5, y: 3.5, w: 9, h: 0.8,
  fontSize: 36, color: colors.accent, align: 'center'
});
slide.addText('15개 모듈로 배우는 실전 Java 개발', {
  x: 0.5, y: 4.5, w: 9, h: 0.5,
  fontSize: 20, color: colors.light, align: 'center'
});

// 목차 슬라이드
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addText('학습 로드맵', {
  x: 0.5, y: 0.3, w: 9, h: 0.6,
  fontSize: 36, bold: true, color: colors.primary
});

const modules = [
  '01. Java 언어 기초',
  '02. JVM과 메모리 관리',
  '03. Java 동시성과 멀티스레딩',
  '04. Spring Core & 의존성 주입',
  '05. Spring Boot 기초',
  '06. Spring Web MVC',
  '07. Spring Data JPA & Database',
  '08. Spring Security & 인증/인가',
  '09. 실전 프로젝트 1: RESTful API',
  '10. Spring WebFlux (반응형)',
  '11. 메시징 & 이벤트 기반',
  '12. 실전 프로젝트 2: 마이크로서비스',
  '13. 테스트 전략',
  '14. 성능 최적화 & 모니터링',
  '15. CI/CD & 배포 전략'
];

const cols = [
  { x: 0.5, y: 1.2, w: 4.5, h: 4 },
  { x: 5.0, y: 1.2, w: 4.5, h: 4 }
];

modules.forEach((mod, i) => {
  const col = i < 8 ? 0 : 1;
  const offset = i < 8 ? i : i - 8;
  slide.addText(`${mod}`, {
    x: cols[col].x,
    y: cols[col].y + offset * 0.5,
    w: cols[col].w,
    h: 0.4,
    fontSize: 14,
    color: colors.text,
    bullet: true
  });
});

// 01. Java 언어 기초
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('01. Java 언어 기초', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('학습 목표', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Golang 경험을 활용한 Java 기본 문법 습득', options: { bullet: true } },
  { text: '객체지향 프로그래밍 개념 이해', options: { bullet: true } },
  { text: '제네릭과 컬렉션 프레임워크', options: { bullet: true } },
  { text: '람다와 Stream API (Java 8+)', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('핵심 내용', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: '개발 환경: JDK 17, IntelliJ IDEA', options: { bullet: true } },
  { text: '기본 문법: 변수, 제어문, 메서드', options: { bullet: true } },
  { text: 'OOP: 클래스, 상속, 인터페이스, 다형성', options: { bullet: true } },
  { text: '제네릭과 컬렉션 (List, Set, Map)', options: { bullet: true } },
  { text: '예외 처리: try-catch-finally', options: { bullet: true } },
  { text: 'Java 최신 기능: Records, Pattern Matching', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 3,
  fontSize: 14, color: colors.text
});

slide.addShape(pptx.ShapeType.rect, {
  x: 0.5, y: 4.3, w: 9, h: 0.8,
  fill: { color: colors.accent }
});
slide.addText('학습 기간: 1-2주  |  난이도: ⭐⭐☆☆☆', {
  x: 0.5, y: 4.4, w: 9, h: 0.6,
  fontSize: 16, color: colors.white, align: 'center'
});

// 02. JVM과 메모리 관리
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('02. JVM과 메모리 관리', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('JVM 아키텍처', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Class Loader', options: { bullet: true } },
  { text: 'Runtime Data Areas (힙, 스택)', options: { bullet: true } },
  { text: 'Execution Engine (JIT 컴파일러)', options: { bullet: true } },
  { text: 'Garbage Collector', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('가비지 컬렉션', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'GC 알고리즘: G1GC, ZGC', options: { bullet: true } },
  { text: 'Young Generation (Eden, Survivor)', options: { bullet: true } },
  { text: 'Old Generation', options: { bullet: true } },
  { text: 'GC 튜닝: 힙 크기, GC 로그', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('JVM 옵션 예시', {
  x: 0.5, y: 4.0, w: 9, h: 0.3,
  fontSize: 16, bold: true, color: colors.primary
});
slide.addText('java -Xms512m -Xmx2g -XX:+UseG1GC MyApp', {
  x: 0.5, y: 4.4, w: 9, h: 0.4,
  fontSize: 13, color: colors.text, fontFace: 'Courier New',
  fill: { color: 'F8F9FA' }
});

slide.addText('학습 기간: 1주  |  난이도: ⭐⭐⭐☆☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 03. Java 동시성과 멀티스레딩
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('03. Java 동시성과 멀티스레딩', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('핵심 개념', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Thread와 Runnable', options: { bullet: true } },
  { text: 'synchronized와 volatile', options: { bullet: true } },
  { text: 'Lock, ReentrantLock', options: { bullet: true } },
  { text: 'Atomic 클래스 (CAS)', options: { bullet: true } },
  { text: 'ExecutorService (Thread Pool)', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2.5,
  fontSize: 14, color: colors.text
});

slide.addText('고급 기능', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'CompletableFuture (비동기)', options: { bullet: true } },
  { text: 'ConcurrentHashMap', options: { bullet: true } },
  { text: 'BlockingQueue (Producer-Consumer)', options: { bullet: true } },
  { text: 'CountDownLatch, CyclicBarrier', options: { bullet: true } },
  { text: 'Semaphore', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2.5,
  fontSize: 14, color: colors.text
});

slide.addShape(pptx.ShapeType.rect, {
  x: 0.5, y: 4.5, w: 9, h: 0.7,
  fill: { color: 'E8F8F5' }
});
slide.addText('💡 Golang goroutine vs Java Thread 비교 학습', {
  x: 0.6, y: 4.6, w: 8.8, h: 0.5,
  fontSize: 14, color: colors.text, italic: true
});

slide.addText('학습 기간: 1-2주  |  난이도: ⭐⭐⭐⭐☆', {
  x: 0.5, y: 5.3, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 04. Spring Core & 의존성 주입
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('04. Spring Core & 의존성 주입', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('IoC & DI', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'IoC (Inversion of Control) 개념', options: { bullet: true } },
  { text: 'DI (Dependency Injection)', options: { bullet: true } },
  { text: '생성자 주입 (권장)', options: { bullet: true } },
  { text: 'ApplicationContext', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('Bean & AOP', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Bean 스코프: Singleton, Prototype', options: { bullet: true } },
  { text: 'Bean 생명주기: @PostConstruct, @PreDestroy', options: { bullet: true } },
  { text: 'AOP (Aspect Oriented Programming)', options: { bullet: true } },
  { text: '@Aspect, @Before, @After', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2.2,
  fontSize: 14, color: colors.text
});

slide.addText('어노테이션', {
  x: 0.5, y: 4.0, w: 9, h: 0.3,
  fontSize: 16, bold: true, color: colors.primary
});
slide.addText('@Component, @Service, @Repository, @Autowired', {
  x: 0.5, y: 4.4, w: 9, h: 0.4,
  fontSize: 13, color: colors.text,
  fill: { color: 'F8F9FA' }
});

slide.addText('학습 기간: 1주  |  난이도: ⭐⭐⭐☆☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 05. Spring Boot 기초
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('05. Spring Boot 기초', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('Spring Boot 특징', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Auto Configuration (자동 설정)', options: { bullet: true } },
  { text: 'Starter Dependencies', options: { bullet: true } },
  { text: 'Embedded Server (Tomcat)', options: { bullet: true } },
  { text: 'Production Ready (Actuator)', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('설정 & 프로파일', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'application.yml 설정', options: { bullet: true } },
  { text: '프로파일: dev, prod', options: { bullet: true } },
  { text: 'Actuator 엔드포인트', options: { bullet: true } },
  { text: 'Maven / Gradle 빌드', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('빠른 시작', {
  x: 0.5, y: 4.0, w: 9, h: 0.3,
  fontSize: 16, bold: true, color: colors.primary
});
slide.addText('Spring Initializr: https://start.spring.io/', {
  x: 0.5, y: 4.4, w: 9, h: 0.4,
  fontSize: 13, color: colors.accent,
  fill: { color: 'F8F9FA' }
});

slide.addText('학습 기간: 1-2주  |  난이도: ⭐⭐☆☆☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 06. Spring Web MVC
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('06. Spring Web MVC', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('REST API 개발', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: '@RestController, @RequestMapping', options: { bullet: true } },
  { text: 'HTTP 메서드: @GetMapping, @PostMapping', options: { bullet: true } },
  { text: '@PathVariable, @RequestParam', options: { bullet: true } },
  { text: 'ResponseEntity', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('검증 & 예외처리', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Bean Validation: @Valid, @NotBlank', options: { bullet: true } },
  { text: '@ExceptionHandler', options: { bullet: true } },
  { text: '@ControllerAdvice (전역 예외)', options: { bullet: true } },
  { text: 'Filter & Interceptor (미들웨어)', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addShape(pptx.ShapeType.rect, {
  x: 0.5, y: 4.0, w: 9, h: 0.8,
  fill: { color: 'FEF9E7' }
});
slide.addText('📌 CORS 설정, 페이징, 함수형 엔드포인트 학습', {
  x: 0.6, y: 4.2, w: 8.8, h: 0.4,
  fontSize: 14, color: colors.text
});

slide.addText('학습 기간: 1주  |  난이도: ⭐⭐⭐☆☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 07. Spring Data JPA
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('07. Spring Data JPA & Database', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('Entity & Repository', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: '@Entity, @Table 매핑', options: { bullet: true } },
  { text: 'JpaRepository 인터페이스', options: { bullet: true } },
  { text: 'Query Methods', options: { bullet: true } },
  { text: '@Query (JPQL, Native SQL)', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('연관관계 & 트랜잭션', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: '1:N, N:1, N:M 매핑', options: { bullet: true } },
  { text: '@Transactional', options: { bullet: true } },
  { text: 'N+1 문제 해결: Fetch Join', options: { bullet: true } },
  { text: '@EntityGraph', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addShape(pptx.ShapeType.rect, {
  x: 0.5, y: 4.0, w: 9, h: 0.8,
  fill: { color: 'FDEDEC' }
});
slide.addText('⚠️ N+1 쿼리 문제는 성능에 치명적! 반드시 해결 방법 숙지', {
  x: 0.6, y: 4.2, w: 8.8, h: 0.4,
  fontSize: 13, color: colors.text
});

slide.addText('학습 기간: 1-2주  |  난이도: ⭐⭐⭐⭐☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 08. Spring Security
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('08. Spring Security & 인증/인가', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('Spring Security 기본', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'SecurityFilterChain 설정', options: { bullet: true } },
  { text: 'PasswordEncoder (BCrypt)', options: { bullet: true } },
  { text: 'Role 기반 접근 제어', options: { bullet: true } },
  { text: '@PreAuthorize, @PostAuthorize', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('JWT 인증', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'JWT 토큰 생성 및 검증', options: { bullet: true } },
  { text: 'JwtAuthenticationFilter', options: { bullet: true } },
  { text: '로그인 API 구현', options: { bullet: true } },
  { text: 'Stateless Session', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('보안 체크리스트', {
  x: 0.5, y: 4.0, w: 9, h: 0.3,
  fontSize: 16, bold: true, color: colors.primary
});
slide.addText('✓ CSRF 방어  ✓ XSS 방어  ✓ SQL Injection 방어  ✓ 비밀번호 암호화', {
  x: 0.5, y: 4.4, w: 9, h: 0.4,
  fontSize: 12, color: colors.text,
  fill: { color: 'F8F9FA' }
});

slide.addText('학습 기간: 1주  |  난이도: ⭐⭐⭐⭐☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 09. 실전 프로젝트 1
slide = pptx.addSlide();
slide.background = { color: colors.accent };
slide.addText('🚀 실전 프로젝트 1', {
  x: 0.5, y: 1.5, w: 9, h: 0.8,
  fontSize: 42, bold: true, color: colors.white, align: 'center'
});
slide.addText('RESTful API 서버 구축', {
  x: 0.5, y: 2.5, w: 9, h: 0.6,
  fontSize: 28, color: colors.light, align: 'center'
});

slide.addText([
  { text: '사용자 관리 (회원가입, 로그인, JWT)', options: { bullet: true } },
  { text: '게시판 CRUD (페이징, 검색, 댓글)', options: { bullet: true } },
  { text: '파일 업로드/다운로드', options: { bullet: true } },
  { text: 'Swagger 문서화', options: { bullet: true } },
  { text: '단위/통합 테스트', options: { bullet: true } },
  { text: 'Docker 컨테이너화', options: { bullet: true } }
], {
  x: 1.5, y: 3.5, w: 7, h: 1.5,
  fontSize: 16, color: colors.white
});

slide.addText('프로젝트 기간: 2주  |  난이도: ⭐⭐⭐⭐☆', {
  x: 0.5, y: 5.2, w: 9, h: 0.4,
  fontSize: 16, bold: true, color: colors.light, align: 'center'
});

// 10. Spring WebFlux
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('10. Spring WebFlux (반응형)', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('Reactive Programming', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Mono (0 또는 1개)', options: { bullet: true } },
  { text: 'Flux (0-N개 스트림)', options: { bullet: true } },
  { text: 'map, flatMap, filter 연산자', options: { bullet: true } },
  { text: 'BackPressure 처리', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('WebFlux vs WebMVC', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'WebMVC: 동기/블로킹', options: { bullet: true } },
  { text: 'WebFlux: 비동기/논블로킹', options: { bullet: true } },
  { text: 'Netty 이벤트 루프', options: { bullet: true } },
  { text: 'R2DBC (Reactive Database)', options: { bullet: true } },
  { text: 'WebClient (비동기 HTTP)', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2.2,
  fontSize: 14, color: colors.text
});

slide.addShape(pptx.ShapeType.rect, {
  x: 0.5, y: 4.3, w: 9, h: 0.7,
  fill: { color: 'E8F8F5' }
});
slide.addText('💡 높은 동시성이 필요한 서비스에 적합 (게임, 실시간 스트리밍)', {
  x: 0.6, y: 4.4, w: 8.8, h: 0.5,
  fontSize: 13, color: colors.text, italic: true
});

slide.addText('학습 기간: 1-2주  |  난이도: ⭐⭐⭐⭐⭐', {
  x: 0.5, y: 5.1, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 11. 메시징 & 이벤트
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('11. 메시징 & 이벤트 기반 아키텍처', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('Spring Kafka', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'KafkaTemplate (Producer)', options: { bullet: true } },
  { text: '@KafkaListener (Consumer)', options: { bullet: true } },
  { text: 'Topic & Partition', options: { bullet: true } },
  { text: 'Consumer Group', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('이벤트 기반 패턴', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Domain Event 발행', options: { bullet: true } },
  { text: '@EventListener (Spring)', options: { bullet: true } },
  { text: 'Outbox 패턴 (트랜잭션 안전)', options: { bullet: true } },
  { text: 'SAGA 패턴', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('이벤트 기반 아키텍처 장점', {
  x: 0.5, y: 4.0, w: 9, h: 0.3,
  fontSize: 16, bold: true, color: colors.primary
});
slide.addText('✓ 느슨한 결합  ✓ 확장성  ✓ 비동기 처리  ✓ 장애 격리', {
  x: 0.5, y: 4.4, w: 9, h: 0.4,
  fontSize: 13, color: colors.text,
  fill: { color: 'F8F9FA' }
});

slide.addText('학습 기간: 1-2주  |  난이도: ⭐⭐⭐⭐☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 12. 실전 프로젝트 2
slide = pptx.addSlide();
slide.background = { color: colors.primary };
slide.addText('🏗️ 실전 프로젝트 2', {
  x: 0.5, y: 1.2, w: 9, h: 0.8,
  fontSize: 42, bold: true, color: colors.white, align: 'center'
});
slide.addText('마이크로서비스 아키텍처', {
  x: 0.5, y: 2.2, w: 9, h: 0.6,
  fontSize: 28, color: colors.accent, align: 'center'
});

slide.addText('서비스 구성', {
  x: 0.5, y: 3.2, w: 4, h: 0.3,
  fontSize: 18, bold: true, color: colors.light
});
slide.addText([
  { text: 'API Gateway', options: { bullet: true } },
  { text: 'User Service', options: { bullet: true } },
  { text: 'Game Service', options: { bullet: true } },
  { text: 'Leaderboard Service', options: { bullet: true } }
], {
  x: 0.5, y: 3.6, w: 4, h: 1.2,
  fontSize: 14, color: colors.light
});

slide.addText('인프라', {
  x: 5.0, y: 3.2, w: 4.5, h: 0.3,
  fontSize: 18, bold: true, color: colors.light
});
slide.addText([
  { text: 'Spring Cloud Gateway', options: { bullet: true } },
  { text: 'Eureka (Service Discovery)', options: { bullet: true } },
  { text: 'Resilience4j (Circuit Breaker)', options: { bullet: true } },
  { text: 'Kubernetes 배포', options: { bullet: true } }
], {
  x: 5.0, y: 3.6, w: 4.5, h: 1.2,
  fontSize: 14, color: colors.light
});

slide.addText('프로젝트 기간: 3-4주  |  난이도: ⭐⭐⭐⭐⭐', {
  x: 0.5, y: 5.2, w: 9, h: 0.4,
  fontSize: 16, bold: true, color: colors.accent, align: 'center'
});

// 13. 테스트 전략
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('13. 테스트 전략', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('단위 테스트', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'JUnit 5', options: { bullet: true } },
  { text: 'Mockito (Mock, Stub)', options: { bullet: true } },
  { text: 'AssertJ (유창한 단언)', options: { bullet: true } },
  { text: 'Test Coverage (JaCoCo)', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('통합 & API 테스트', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: '@SpringBootTest', options: { bullet: true } },
  { text: 'MockMvc (컨트롤러 테스트)', options: { bullet: true } },
  { text: 'Testcontainers (DB 테스트)', options: { bullet: true } },
  { text: 'RestAssured (API 테스트)', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addShape(pptx.ShapeType.rect, {
  x: 0.5, y: 4.0, w: 9, h: 0.8,
  fill: { color: 'FEF9E7' }
});
slide.addText('🎯 목표: 테스트 커버리지 80% 이상 달성', {
  x: 0.6, y: 4.2, w: 8.8, h: 0.4,
  fontSize: 15, color: colors.text, bold: true
});

slide.addText('학습 기간: 1주  |  난이도: ⭐⭐⭐☆☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 14. 성능 최적화
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('14. 성능 최적화 & 모니터링', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('최적화 기법', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'JVM 튜닝 (힙, GC)', options: { bullet: true } },
  { text: 'DB 쿼리 최적화', options: { bullet: true } },
  { text: 'Redis 캐싱', options: { bullet: true } },
  { text: 'Connection Pool 튜닝', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('모니터링', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Micrometer + Prometheus', options: { bullet: true } },
  { text: 'Grafana 대시보드', options: { bullet: true } },
  { text: 'APM (New Relic, Datadog)', options: { bullet: true } },
  { text: '구조화된 로깅 (ELK Stack)', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('성능 지표', {
  x: 0.5, y: 4.0, w: 9, h: 0.3,
  fontSize: 16, bold: true, color: colors.primary
});
slide.addText('응답시간 (Latency) • 처리량 (Throughput) • 에러율 • CPU/메모리 사용률', {
  x: 0.5, y: 4.4, w: 9, h: 0.4,
  fontSize: 13, color: colors.text,
  fill: { color: 'F8F9FA' }
});

slide.addText('학습 기간: 1-2주  |  난이도: ⭐⭐⭐⭐☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 15. CI/CD & 배포
slide = pptx.addSlide();
slide.background = { color: colors.white };
slide.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 10, h: 0.8,
  fill: { color: colors.primary }
});
slide.addText('15. CI/CD & 배포 전략', {
  x: 0.5, y: 0.2, w: 9, h: 0.4,
  fontSize: 28, bold: true, color: colors.white
});

slide.addText('CI/CD 파이프라인', {
  x: 0.5, y: 1.2, w: 4, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'GitHub Actions', options: { bullet: true } },
  { text: '자동 빌드 & 테스트', options: { bullet: true } },
  { text: 'Docker 이미지 생성', options: { bullet: true } },
  { text: 'Container Registry 푸시', options: { bullet: true } }
], {
  x: 0.5, y: 1.7, w: 4, h: 2,
  fontSize: 14, color: colors.text
});

slide.addText('배포 전략', {
  x: 5.0, y: 1.2, w: 4.5, h: 0.4,
  fontSize: 20, bold: true, color: colors.primary
});
slide.addText([
  { text: 'Docker Compose (개발)', options: { bullet: true } },
  { text: 'Kubernetes (프로덕션)', options: { bullet: true } },
  { text: 'Helm Chart', options: { bullet: true } },
  { text: '무중단 배포 (Blue-Green)', options: { bullet: true } }
], {
  x: 5.0, y: 1.7, w: 4.5, h: 2,
  fontSize: 14, color: colors.text
});

slide.addShape(pptx.ShapeType.rect, {
  x: 0.5, y: 4.0, w: 9, h: 0.8,
  fill: { color: 'E8F8F5' }
});
slide.addText('🚀 환경별 설정: dev → staging → production', {
  x: 0.6, y: 4.2, w: 8.8, h: 0.4,
  fontSize: 14, color: colors.text, bold: true
});

slide.addText('학습 기간: 1주  |  난이도: ⭐⭐⭐☆☆', {
  x: 0.5, y: 5.0, w: 9, h: 0.4,
  fontSize: 14, color: colors.secondary, align: 'center'
});

// 마무리 슬라이드
slide = pptx.addSlide();
slide.background = { color: colors.accent };
slide.addText('🎉 학습 완료!', {
  x: 0.5, y: 1.5, w: 9, h: 0.8,
  fontSize: 48, bold: true, color: colors.white, align: 'center'
});

slide.addText('다음 단계', {
  x: 1.5, y: 2.8, w: 7, h: 0.4,
  fontSize: 24, bold: true, color: colors.white
});

slide.addText([
  { text: '포트폴리오 프로젝트 완성', options: { bullet: true } },
  { text: '오픈소스 기여', options: { bullet: true } },
  { text: '기술 블로그 작성', options: { bullet: true } },
  { text: '이직 준비 & 면접 연습', options: { bullet: true } }
], {
  x: 1.5, y: 3.3, w: 7, h: 1.5,
  fontSize: 18, color: colors.white
});

slide.addText('Java & Spring 마스터로 가는 여정을 응원합니다!', {
  x: 0.5, y: 5.0, w: 9, h: 0.5,
  fontSize: 20, color: colors.light, align: 'center', italic: true
});

// 파일 저장
pptx.writeFile({ fileName: 'Java-Spring-Roadmap.pptx' })
  .then(() => {
    console.log('✅ 프레젠테이션이 성공적으로 생성되었습니다!');
    console.log('📁 파일명: Java-Spring-Roadmap.pptx');
  })
  .catch(err => {
    console.error('❌ 오류 발생:', err);
  });
