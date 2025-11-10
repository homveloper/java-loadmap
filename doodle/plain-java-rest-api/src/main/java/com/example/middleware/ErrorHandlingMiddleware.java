package com.example.middleware;

import com.example.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 에러 핸들링 미들웨어
 *
 * 모든 예외를 포착하여 일관된 JSON 에러 응답을 반환합니다.
 *
 * 에러 응답 형식:
 * <pre>
 * {
 *   "error": "Resource not found",
 *   "status": 404,
 *   "path": "/api/users/999",
 *   "timestamp": "2025-11-04T11:30:45"
 * }
 * </pre>
 */
public class ErrorHandlingMiddleware implements Middleware {

    @Override
    public void handle(HttpExchange exchange, Runnable next) throws IOException {
        try {
            // 다음 미들웨어/핸들러 실행
            next.run();

        } catch (RuntimeException e) {
            // RuntimeException 처리
            handleException(exchange, e);

        } catch (Exception e) {
            // 기타 모든 예외 처리
            handleException(exchange, e);
        }
    }

    /**
     * 예외를 HTTP 응답으로 변환
     *
     * @param exchange HTTP 요청/응답
     * @param exception 발생한 예외
     */
    private void handleException(HttpExchange exchange, Exception exception) throws IOException {
        // 예외 타입에 따른 HTTP 상태 코드 결정
        int statusCode = determineStatusCode(exception);
        String errorMessage = exception.getMessage() != null
                ? exception.getMessage()
                : exception.getClass().getSimpleName();

        // 에러 JSON 생성
        String errorJson = buildErrorJson(
                errorMessage,
                statusCode,
                exchange.getRequestURI().getPath()
        );

        // 응답 전송
        sendErrorResponse(exchange, statusCode, errorJson);

        // 스택 트레이스 로깅 (개발 환경용)
        System.err.println("Exception caught by ErrorHandlingMiddleware:");
        exception.printStackTrace();
    }

    /**
     * 예외 타입에 따른 HTTP 상태 코드 결정
     */
    private int determineStatusCode(Exception exception) {
        String exceptionName = exception.getClass().getSimpleName();

        // 일반적인 예외 타입 매핑
        return switch (exceptionName) {
            case "IllegalArgumentException", "NumberFormatException" -> 400; // Bad Request
            case "ResourceNotFoundException", "NoSuchElementException" -> 404; // Not Found
            case "IllegalStateException" -> 409; // Conflict
            case "UnsupportedOperationException" -> 501; // Not Implemented
            default -> 500; // Internal Server Error
        };
    }

    /**
     * 에러 JSON 생성
     */
    private String buildErrorJson(String message, int status, String path) {
        String timestamp = java.time.LocalDateTime.now().toString();

        return String.format(
                "{\"error\":\"%s\",\"status\":%d,\"path\":\"%s\",\"timestamp\":\"%s\"}",
                escapeJson(message),
                status,
                escapeJson(path),
                timestamp
        );
    }

    /**
     * JSON 문자열 이스케이프
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 에러 응답 전송
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
