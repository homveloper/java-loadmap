package com.example.router;

import com.example.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 요청/응답 컨텍스트
 *
 * Golang의 Gin Context처럼 요청과 응답을 쉽게 처리할 수 있게 해주는 래퍼 클래스
 */
public class Context {
    private final HttpExchange exchange;
    private final Map<String, String> pathParams;
    private int statusCode = 200;

    public Context(HttpExchange exchange) {
        this.exchange = exchange;
        this.pathParams = new HashMap<>();
    }

    // ========== Request Methods ==========

    /**
     * HTTP 메서드 가져오기 (GET, POST, PUT, DELETE 등)
     */
    public String method() {
        return exchange.getRequestMethod();
    }

    /**
     * 요청 경로 가져오기
     */
    public String path() {
        return exchange.getRequestURI().getPath();
    }

    /**
     * Path 파라미터 가져오기
     * 예: /api/users/:id -> ctx.pathParam("id")
     */
    public String pathParam(String name) {
        return pathParams.get(name);
    }

    /**
     * Path 파라미터를 Long으로 가져오기
     */
    public Long pathParamAsLong(String name) {
        String value = pathParams.get(name);
        return value != null ? Long.parseLong(value) : null;
    }

    /**
     * Path 파라미터 설정 (Router에서 내부적으로 사용)
     */
    void setPathParam(String name, String value) {
        pathParams.put(name, value);
    }

    /**
     * 요청 본문을 문자열로 읽기
     */
    public String body() throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
    }

    /**
     * 요청 본문을 객체로 변환
     * JSON → Java 객체
     */
    public <T> T bodyAs(Class<T> clazz) throws IOException {
        String json = body();

        // User 클래스인 경우
        if (clazz.getSimpleName().equals("User")) {
            return (T) JsonUtil.userFromJson(json);
        }
        // Post 클래스인 경우
        else if (clazz.getSimpleName().equals("Post")) {
            return (T) JsonUtil.postFromJson(json);
        }

        throw new IllegalArgumentException("Unsupported class: " + clazz.getName());
    }

    /**
     * 쿼리 파라미터 가져오기
     * 예: /api/users?name=홍길동 -> ctx.query("name")
     */
    public String query(String name) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(name)) {
                return keyValue[1];
            }
        }
        return null;
    }

    // ========== Response Methods ==========

    /**
     * HTTP 상태 코드 설정
     *
     * 체이닝 가능: ctx.status(201).json(user);
     */
    public Context status(int code) {
        this.statusCode = code;
        return this;
    }

    /**
     * JSON 응답 전송
     */
    public void json(Object obj) throws IOException {
        String json;

        if (obj instanceof String) {
            json = (String) obj;
        } else if (obj instanceof java.util.List) {
            // List인 경우
            java.util.List<?> list = (java.util.List<?>) obj;
            if (!list.isEmpty() && list.get(0).getClass().getSimpleName().equals("User")) {
                json = JsonUtil.toJson((java.util.List) list);
            } else if (!list.isEmpty() && list.get(0).getClass().getSimpleName().equals("Post")) {
                json = JsonUtil.postsToJson((java.util.List) list);
            } else {
                json = "[]";
            }
        } else {
            // 단일 객체인 경우
            String className = obj.getClass().getSimpleName();
            if (className.equals("User")) {
                json = JsonUtil.toJson((com.example.model.User) obj);
            } else if (className.equals("Post")) {
                json = JsonUtil.postToJson((com.example.model.Post) obj);
            } else {
                json = obj.toString();
            }
        }

        sendJson(json);
    }

    /**
     * 텍스트 응답 전송
     */
    public void text(String text) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        sendResponse(text.getBytes("UTF-8"));
    }

    /**
     * 에러 응답 전송
     */
    public void error(int statusCode, String message) throws IOException {
        this.statusCode = statusCode;
        String errorJson = JsonUtil.errorJson(message);
        sendJson(errorJson);
    }

    /**
     * No Content 응답 (204)
     */
    public void noContent() throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }

    // ========== Internal Methods ==========

    private void sendJson(String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        // CORS 헤더
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        sendResponse(json.getBytes("UTF-8"));
    }

    private void sendResponse(byte[] bytes) throws IOException {
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * HttpExchange 객체 가져오기 (필요시)
     */
    public HttpExchange exchange() {
        return exchange;
    }
}
