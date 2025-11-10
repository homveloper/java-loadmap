package com.example.handler;

import com.example.model.User;
import com.example.util.JsonUtil;
import com.example.util.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User API 핸들러
 *
 * /api/users 경로에 대한 모든 HTTP 요청을 처리합니다.
 */
public class UserHandler implements HttpHandler {

    // In-memory 데이터 저장소 (Thread-safe)
    private static final Map<Long, User> users = new ConcurrentHashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    // 초기 데이터 설정
    static {
        User user1 = new User(idGenerator.getAndIncrement(), "홍길동", "hong@example.com");
        User user2 = new User(idGenerator.getAndIncrement(), "김철수", "kim@example.com");
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // CORS preflight 처리
        if ("OPTIONS".equals(method)) {
            Response.sendNoContent(exchange);
            return;
        }

        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "POST" -> handlePost(exchange);
                case "PUT" -> handlePut(exchange, path);
                case "DELETE" -> handleDelete(exchange, path);
                default -> Response.sendError(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Response.sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * GET 요청 처리
     *
     * GET /api/users - 모든 사용자 조회
     * GET /api/users/{id} - 특정 사용자 조회
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id != null) {
            // 특정 사용자 조회
            User user = users.get(id);
            if (user == null) {
                Response.sendError(exchange, 404, "User not found");
            } else {
                String json = JsonUtil.toJson(user);
                Response.sendJson(exchange, 200, json);
            }
        } else {
            // 모든 사용자 조회
            List<User> userList = new ArrayList<>(users.values());
            String json = JsonUtil.toJson(userList);
            Response.sendJson(exchange, 200, json);
        }
    }

    /**
     * POST 요청 처리
     *
     * POST /api/users - 새 사용자 생성
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        // 요청 본문 읽기
        String body = Response.readRequestBody(exchange);

        // JSON → User 객체 변환
        User newUser = JsonUtil.userFromJson(body);

        if (newUser.getName() == null || newUser.getEmail() == null) {
            Response.sendError(exchange, 400, "Name and email are required");
            return;
        }

        // ID 생성 및 저장
        Long id = idGenerator.getAndIncrement();
        newUser.setId(id);
        users.put(id, newUser);

        // 응답
        String json = JsonUtil.toJson(newUser);
        Response.sendJson(exchange, 201, json);
    }

    /**
     * PUT 요청 처리
     *
     * PUT /api/users/{id} - 사용자 정보 수정
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id == null) {
            Response.sendError(exchange, 400, "User ID is required");
            return;
        }

        User existingUser = users.get(id);
        if (existingUser == null) {
            Response.sendError(exchange, 404, "User not found");
            return;
        }

        // 요청 본문 읽기
        String body = Response.readRequestBody(exchange);

        // JSON → User 객체 변환
        User updatedUser = JsonUtil.userFromJson(body);

        // 기존 사용자 정보 업데이트
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }

        // 응답
        String json = JsonUtil.toJson(existingUser);
        Response.sendJson(exchange, 200, json);
    }

    /**
     * DELETE 요청 처리
     *
     * DELETE /api/users/{id} - 사용자 삭제
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id == null) {
            Response.sendError(exchange, 400, "User ID is required");
            return;
        }

        User removedUser = users.remove(id);
        if (removedUser == null) {
            Response.sendError(exchange, 404, "User not found");
        } else {
            Response.sendNoContent(exchange);
        }
    }
}
