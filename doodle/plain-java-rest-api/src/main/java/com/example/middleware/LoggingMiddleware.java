package com.example.middleware;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 로깅 미들웨어
 *
 * HTTP 요청/응답을 로깅합니다.
 * 형식: [2025-11-04 11:30:45] GET /api/products 200 (15ms)
 */
public class LoggingMiddleware implements Middleware {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void handle(HttpExchange exchange, Runnable next) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        long startTime = System.currentTimeMillis();

        // 요청 시작 로그
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.printf("[%s] --> %s %s%n", timestamp, method, path);

        try {
            // 다음 미들웨어/핸들러 실행
            next.run();

            // 응답 완료 로그
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponseCode();
            System.out.printf("[%s] <-- %s %s %d (%dms)%n",
                    timestamp, method, path, statusCode, duration);

        } catch (Exception e) {
            // 에러 로그
            long duration = System.currentTimeMillis() - startTime;
            System.err.printf("[%s] <-- %s %s ERROR (%dms): %s%n",
                    timestamp, method, path, duration, e.getMessage());
            throw e;
        }
    }
}
