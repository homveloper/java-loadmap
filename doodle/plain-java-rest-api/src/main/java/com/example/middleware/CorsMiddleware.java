package com.example.middleware;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * CORS 미들웨어
 *
 * Cross-Origin Resource Sharing 헤더를 추가하여
 * 다른 도메인에서의 API 호출을 허용합니다.
 */
public class CorsMiddleware implements Middleware {

    private final String allowOrigin;
    private final String allowMethods;
    private final String allowHeaders;

    /**
     * 기본 CORS 설정
     * - Origin: * (모든 도메인 허용)
     * - Methods: GET, POST, PUT, DELETE, OPTIONS
     * - Headers: Content-Type, Authorization
     */
    public CorsMiddleware() {
        this("*",
             "GET, POST, PUT, DELETE, OPTIONS",
             "Content-Type, Authorization");
    }

    /**
     * 커스텀 CORS 설정
     *
     * @param allowOrigin 허용할 Origin
     * @param allowMethods 허용할 HTTP 메서드
     * @param allowHeaders 허용할 헤더
     */
    public CorsMiddleware(String allowOrigin, String allowMethods, String allowHeaders) {
        this.allowOrigin = allowOrigin;
        this.allowMethods = allowMethods;
        this.allowHeaders = allowHeaders;
    }

    @Override
    public void handle(HttpExchange exchange, Runnable next) throws IOException {
        // CORS 헤더 추가
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", allowOrigin);
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", allowMethods);
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", allowHeaders);
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");

        // OPTIONS preflight 요청 처리
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // 다음 미들웨어/핸들러 실행
        next.run();
    }
}
