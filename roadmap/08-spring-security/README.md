# 08. Spring Security & 인증/인가

> **학습 기간**: 1주
> **난이도**: ⭐⭐⭐⭐☆
> **전제 조건**: Spring Web MVC

## 📚 학습 목표

JWT 기반 인증 시스템을 구축합니다.

## 🎯 핵심 내용

### 1. Spring Security 기본
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
}
```

### 2. JWT 인증
```java
// JWT 토큰 생성
public String generateToken(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24시간
        .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
        .compact();
}

// JWT 검증 필터
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            // SecurityContext에 인증 정보 설정
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

### 3. 로그인 API
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        String token = jwtUtil.generateToken(authentication.getName());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
```

### 4. Password Encoding
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// 사용
String encodedPassword = passwordEncoder.encode("rawPassword");
boolean matches = passwordEncoder.matches("rawPassword", encodedPassword);
```

### 5. Role 기반 접근 제어
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.delete(id);
    }
}
```

## 📖 학습 리소스

- [Spring Security 공식 문서](https://docs.spring.io/spring-security/reference/index.html)
- [JWT.io](https://jwt.io/)

## ✅ 체크리스트

- [ ] Spring Security 설정
- [ ] JWT 토큰 생성/검증
- [ ] 로그인/회원가입 API
- [ ] Password Encoding
- [ ] Role 기반 인가

## 🚀 다음 단계

**→ [09. 실전 프로젝트 1: RESTful API 서버](../09-project-rest-api/)**
