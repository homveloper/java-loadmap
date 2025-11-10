package com.example;

import com.example.controller.ProductController;
import com.example.handler.PostHandler;
import com.example.handler.UserHandler;
import com.example.middleware.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Plain Java REST API Server
 *
 * Spring 없이 순수 Java만으로 구현한 REST API 서버
 * JDK 내장 HttpServer를 사용합니다.
 *
 * 실행 방법:
 * 1. 컴파일: javac -d out src/main/java/com/example/*.java ...
 * 2. 실행: java -cp out com.example.RestApiServer
 * 3. 또는: java src/main/java/com/example/RestApiServer.java (Java 11+)
 *
 * API 엔드포인트:
 * - GET    /api/users       - 모든 사용자 조회
 * - GET    /api/users/{id}  - 특정 사용자 조회
 * - POST   /api/users       - 새 사용자 생성
 * - PUT    /api/users/{id}  - 사용자 수정
 * - DELETE /api/users/{id}  - 사용자 삭제
 *
 * - GET    /api/posts       - 모든 게시글 조회
 * - GET    /api/posts/{id}  - 특정 게시글 조회
 * - POST   /api/posts       - 새 게시글 생성
 * - PUT    /api/posts/{id}  - 게시글 수정
 * - DELETE /api/posts/{id}  - 게시글 삭제
 *
 * - GET    /api/products       - 모든 상품 조회 (함수형 핸들러)
 * - GET    /api/products/{id}  - 특정 상품 조회 (함수형 핸들러)
 * - POST   /api/products       - 새 상품 생성 (함수형 핸들러)
 * - PUT    /api/products/{id}  - 상품 수정 (함수형 핸들러)
 * - DELETE /api/products/{id}  - 상품 삭제 (함수형 핸들러)
 */
public class RestApiServer {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            // HttpServer 생성 (포트 8080, 백로그 0 = 기본값 사용)
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // ========== 미들웨어 설정 ==========
            // Logging -> CORS -> ErrorHandling 순서로 적용
            MiddlewareChain middlewares = new MiddlewareChain()
                    .use(new LoggingMiddleware())
                    .use(new CorsMiddleware())
                    .use(new ErrorHandlingMiddleware());

            // 컨텍스트(경로) 등록 - 미들웨어를 적용한 핸들러

            // 클래스 기반 핸들러 (HttpHandler 상속) + 미들웨어
            server.createContext("/api/users", middlewares.wrap(new UserHandler()));
            server.createContext("/api/posts", middlewares.wrap(new PostHandler()));

            // 함수형 핸들러 (메서드 참조) + 미들웨어
            server.createContext("/api/products", middlewares.wrap(ProductController::handle));

            // 기본 루트 경로 - 서버 정보 제공
            server.createContext("/", exchange -> {
                String response = """
                    {
                      "name": "Plain Java REST API Server",
                      "version": "1.0.0",
                      "description": "Spring 없이 순수 Java만으로 구현한 REST API 서버",
                      "endpoints": [
                        "GET /api/users - 모든 사용자 조회 (클래스 핸들러)",
                        "GET /api/users/{id} - 특정 사용자 조회 (클래스 핸들러)",
                        "POST /api/users - 새 사용자 생성 (클래스 핸들러)",
                        "PUT /api/users/{id} - 사용자 수정 (클래스 핸들러)",
                        "DELETE /api/users/{id} - 사용자 삭제 (클래스 핸들러)",
                        "GET /api/posts - 모든 게시글 조회 (클래스 핸들러)",
                        "GET /api/posts/{id} - 특정 게시글 조회 (클래스 핸들러)",
                        "POST /api/posts - 새 게시글 생성 (클래스 핸들러)",
                        "PUT /api/posts/{id} - 게시글 수정 (클래스 핸들러)",
                        "DELETE /api/posts/{id} - 게시글 삭제 (클래스 핸들러)",
                        "GET /api/products - 모든 상품 조회 (함수형 핸들러)",
                        "GET /api/products/{id} - 특정 상품 조회 (함수형 핸들러)",
                        "POST /api/products - 새 상품 생성 (함수형 핸들러)",
                        "PUT /api/products/{id} - 상품 수정 (함수형 핸들러)",
                        "DELETE /api/products/{id} - 상품 삭제 (함수형 핸들러)"
                      ]
                    }
                    """;

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] bytes = response.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();
            });

            // Executor 설정 (null = 기본 Executor 사용)
            server.setExecutor(null);

            // 서버 시작
            server.start();

            System.out.println("=".repeat(60));
            System.out.println("🚀 Plain Java REST API Server Started!");
            System.out.println("=".repeat(60));
            System.out.println("Server running on: http://localhost:" + PORT);
            System.out.println();
            System.out.println("Middlewares enabled:");
            System.out.println("  ✓ Logging (요청/응답 로깅)");
            System.out.println("  ✓ CORS (Cross-Origin 허용)");
            System.out.println("  ✓ Error Handling (통합 에러 처리)");
            System.out.println();
            System.out.println("Available endpoints:");
            System.out.println("  - http://localhost:" + PORT + "/");
            System.out.println("  - http://localhost:" + PORT + "/api/users (클래스 핸들러)");
            System.out.println("  - http://localhost:" + PORT + "/api/posts (클래스 핸들러)");
            System.out.println("  - http://localhost:" + PORT + "/api/products (함수형 핸들러)");
            System.out.println();
            System.out.println("Try:");
            System.out.println("  curl http://localhost:" + PORT + "/api/users");
            System.out.println("  curl http://localhost:" + PORT + "/api/products");
            System.out.println();
            System.out.println("Press Ctrl+C to stop the server");
            System.out.println("=".repeat(60));

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
