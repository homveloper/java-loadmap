package com.example.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * HTTP 응답 헬퍼 클래스
 *
 * HttpExchange를 통한 응답 전송을 간편하게 처리합니다.
 */
public class Response {

    /**
     * JSON 응답 전송
     *
     * @param exchange HttpExchange 객체
     * @param statusCode HTTP 상태 코드
     * @param json JSON 문자열
     */
    public static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        // Content-Type 헤더 설정
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        // CORS 헤더 추가 (개발 편의를 위해)
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        // 응답 전송
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * 에러 응답 전송
     *
     * @param exchange HttpExchange 객체
     * @param statusCode HTTP 상태 코드
     * @param message 에러 메시지
     */
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String errorJson = JsonUtil.errorJson(message);
        sendJson(exchange, statusCode, errorJson);
    }

    /**
     * 성공 메시지 응답 전송
     *
     * @param exchange HttpExchange 객체
     * @param message 성공 메시지
     */
    public static void sendMessage(HttpExchange exchange, String message) throws IOException {
        String messageJson = JsonUtil.messageJson(message);
        sendJson(exchange, 200, messageJson);
    }

    /**
     * 204 No Content 응답 전송
     *
     * @param exchange HttpExchange 객체
     */
    public static void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * 요청 본문 읽기
     *
     * @param exchange HttpExchange 객체
     * @return 요청 본문 문자열
     */
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
    }

    /**
     * Path에서 ID 추출
     *
     * 예: /api/users/123 -> 123
     *
     * @param path 요청 경로
     * @return ID 값 (없으면 null)
     */
    public static Long extractId(String path) {
        String[] parts = path.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            try {
                return Long.parseLong(lastPart);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
