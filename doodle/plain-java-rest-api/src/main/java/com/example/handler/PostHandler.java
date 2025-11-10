package com.example.handler;

import com.example.model.Post;
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
 * Post API 핸들러
 *
 * /api/posts 경로에 대한 모든 HTTP 요청을 처리합니다.
 */
public class PostHandler implements HttpHandler {

    // In-memory 데이터 저장소 (Thread-safe)
    private static final Map<Long, Post> posts = new ConcurrentHashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    // 초기 데이터 설정
    static {
        Post post1 = new Post(
                idGenerator.getAndIncrement(),
                "Plain Java로 REST API 만들기",
                "Spring 없이도 REST API를 만들 수 있습니다!",
                1L
        );
        Post post2 = new Post(
                idGenerator.getAndIncrement(),
                "HttpServer 활용하기",
                "JDK 내장 HttpServer는 가볍고 강력합니다.",
                1L
        );
        posts.put(post1.getId(), post1);
        posts.put(post2.getId(), post2);
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
     * GET /api/posts - 모든 게시글 조회
     * GET /api/posts/{id} - 특정 게시글 조회
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id != null) {
            // 특정 게시글 조회
            Post post = posts.get(id);
            if (post == null) {
                Response.sendError(exchange, 404, "Post not found");
            } else {
                String json = JsonUtil.postToJson(post);
                Response.sendJson(exchange, 200, json);
            }
        } else {
            // 모든 게시글 조회
            List<Post> postList = new ArrayList<>(posts.values());
            String json = JsonUtil.postsToJson(postList);
            Response.sendJson(exchange, 200, json);
        }
    }

    /**
     * POST 요청 처리
     *
     * POST /api/posts - 새 게시글 생성
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        // 요청 본문 읽기
        String body = Response.readRequestBody(exchange);

        // JSON → Post 객체 변환
        Post newPost = JsonUtil.postFromJson(body);

        if (newPost.getTitle() == null || newPost.getContent() == null) {
            Response.sendError(exchange, 400, "Title and content are required");
            return;
        }

        // ID 생성 및 저장
        Long id = idGenerator.getAndIncrement();
        newPost.setId(id);
        posts.put(id, newPost);

        // 응답
        String json = JsonUtil.postToJson(newPost);
        Response.sendJson(exchange, 201, json);
    }

    /**
     * PUT 요청 처리
     *
     * PUT /api/posts/{id} - 게시글 수정
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id == null) {
            Response.sendError(exchange, 400, "Post ID is required");
            return;
        }

        Post existingPost = posts.get(id);
        if (existingPost == null) {
            Response.sendError(exchange, 404, "Post not found");
            return;
        }

        // 요청 본문 읽기
        String body = Response.readRequestBody(exchange);

        // JSON → Post 객체 변환
        Post updatedPost = JsonUtil.postFromJson(body);

        // 기존 게시글 정보 업데이트
        if (updatedPost.getTitle() != null) {
            existingPost.setTitle(updatedPost.getTitle());
        }
        if (updatedPost.getContent() != null) {
            existingPost.setContent(updatedPost.getContent());
        }

        // 응답
        String json = JsonUtil.postToJson(existingPost);
        Response.sendJson(exchange, 200, json);
    }

    /**
     * DELETE 요청 처리
     *
     * DELETE /api/posts/{id} - 게시글 삭제
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id == null) {
            Response.sendError(exchange, 400, "Post ID is required");
            return;
        }

        Post removedPost = posts.remove(id);
        if (removedPost == null) {
            Response.sendError(exchange, 404, "Post not found");
        } else {
            Response.sendNoContent(exchange);
        }
    }
}
