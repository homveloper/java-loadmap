package com.example.controller;

import com.example.model.Product;
import com.example.util.JsonUtil;
import com.example.util.Response;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Product API Controller (함수형 스타일)
 *
 * HttpHandler를 상속하지 않고, 정적 메서드만으로 구현
 * 각 메서드는 HttpExchange를 받아서 처리
 *
 * 장점:
 * - 클래스 상속 없이 간단한 함수로 처리
 * - 메서드 참조나 람다로 라우팅 등록 가능
 * - 테스트하기 쉬움
 */
public class ProductController {

    // In-memory 데이터 저장소 (Thread-safe)
    private static final Map<Long, Product> products = new ConcurrentHashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    // 초기 데이터 설정
    static {
        Product p1 = new Product(idGenerator.getAndIncrement(), "노트북", 1500000, 10);
        Product p2 = new Product(idGenerator.getAndIncrement(), "마우스", 30000, 50);
        Product p3 = new Product(idGenerator.getAndIncrement(), "키보드", 80000, 30);
        products.put(p1.getId(), p1);
        products.put(p2.getId(), p2);
        products.put(p3.getId(), p3);
    }

    /**
     * 메인 핸들러 - HTTP 메서드별로 분기
     *
     * 이 메서드를 메서드 참조로 등록:
     * server.createContext("/api/products", ProductController::handle);
     */
    public static void handle(HttpExchange exchange) throws IOException {
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
     * GET /api/products - 모든 상품 조회
     * GET /api/products/{id} - 특정 상품 조회
     */
    private static void handleGet(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id != null) {
            // 특정 상품 조회
            Product product = products.get(id);
            if (product == null) {
                Response.sendError(exchange, 404, "Product not found");
            } else {
                String json = JsonUtil.productToJson(product);
                Response.sendJson(exchange, 200, json);
            }
        } else {
            // 모든 상품 조회
            List<Product> productList = new ArrayList<>(products.values());
            String json = JsonUtil.productsToJson(productList);
            Response.sendJson(exchange, 200, json);
        }
    }

    /**
     * POST 요청 처리
     *
     * POST /api/products - 새 상품 생성
     */
    private static void handlePost(HttpExchange exchange) throws IOException {
        // 요청 본문 읽기
        String body = Response.readRequestBody(exchange);

        // JSON → Product 객체 변환
        Product newProduct = JsonUtil.productFromJson(body);

        if (newProduct.getName() == null || newProduct.getPrice() == null) {
            Response.sendError(exchange, 400, "Name and price are required");
            return;
        }

        // ID 생성 및 저장
        Long id = idGenerator.getAndIncrement();
        newProduct.setId(id);

        // stock이 없으면 0으로 설정
        if (newProduct.getStock() == null) {
            newProduct.setStock(0);
        }

        products.put(id, newProduct);

        // 응답
        String json = JsonUtil.productToJson(newProduct);
        Response.sendJson(exchange, 201, json);
    }

    /**
     * PUT 요청 처리
     *
     * PUT /api/products/{id} - 상품 정보 수정
     */
    private static void handlePut(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id == null) {
            Response.sendError(exchange, 400, "Product ID is required");
            return;
        }

        Product existingProduct = products.get(id);
        if (existingProduct == null) {
            Response.sendError(exchange, 404, "Product not found");
            return;
        }

        // 요청 본문 읽기
        String body = Response.readRequestBody(exchange);

        // JSON → Product 객체 변환
        Product updatedProduct = JsonUtil.productFromJson(body);

        // 기존 상품 정보 업데이트
        if (updatedProduct.getName() != null) {
            existingProduct.setName(updatedProduct.getName());
        }
        if (updatedProduct.getPrice() != null) {
            existingProduct.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getStock() != null) {
            existingProduct.setStock(updatedProduct.getStock());
        }

        // 응답
        String json = JsonUtil.productToJson(existingProduct);
        Response.sendJson(exchange, 200, json);
    }

    /**
     * DELETE 요청 처리
     *
     * DELETE /api/products/{id} - 상품 삭제
     */
    private static void handleDelete(HttpExchange exchange, String path) throws IOException {
        Long id = Response.extractId(path);

        if (id == null) {
            Response.sendError(exchange, 400, "Product ID is required");
            return;
        }

        Product removedProduct = products.remove(id);
        if (removedProduct == null) {
            Response.sendError(exchange, 404, "Product not found");
        } else {
            Response.sendNoContent(exchange);
        }
    }

    // ========== 개별 함수로도 노출 가능 ==========

    /**
     * 모든 상품 조회 (람다 표현식으로 사용 가능)
     */
    public static void getAll(HttpExchange exchange) throws IOException {
        List<Product> productList = new ArrayList<>(products.values());
        String json = JsonUtil.productsToJson(productList);
        Response.sendJson(exchange, 200, json);
    }

    /**
     * 상품 생성 (람다 표현식으로 사용 가능)
     */
    public static void create(HttpExchange exchange) throws IOException {
        handlePost(exchange);
    }
}
