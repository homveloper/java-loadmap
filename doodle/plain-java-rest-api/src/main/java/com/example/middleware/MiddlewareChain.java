package com.example.middleware;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 미들웨어 체인 빌더
 *
 * 여러 미들웨어를 체인으로 연결하여 순서대로 실행
 *
 * 사용 예:
 * <pre>
 * MiddlewareChain chain = new MiddlewareChain()
 *     .use(new LoggingMiddleware())
 *     .use(new CorsMiddleware())
 *     .use(new ErrorHandlingMiddleware());
 *
 * server.createContext("/api/users", chain.wrap(UserController::handle));
 * </pre>
 */
public class MiddlewareChain {
    private final List<Middleware> middlewares = new ArrayList<>();

    /**
     * 미들웨어 추가 (체이닝 가능)
     *
     * @param middleware 추가할 미들웨어
     * @return this (체이닝을 위해)
     */
    public MiddlewareChain use(Middleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    /**
     * 최종 핸들러를 미들웨어로 감싸기
     *
     * @param finalHandler 최종 실행될 핸들러
     * @return 미들웨어가 적용된 HttpHandler
     */
    public HttpHandler wrap(HttpHandler finalHandler) {
        return exchange -> {
            executeChain(exchange, 0, finalHandler);
        };
    }

    /**
     * 미들웨어 체인 재귀 실행
     *
     * @param exchange HTTP 요청/응답
     * @param index 현재 실행할 미들웨어 인덱스
     * @param finalHandler 최종 핸들러
     */
    private void executeChain(HttpExchange exchange, int index, HttpHandler finalHandler) throws IOException {
        if (index >= middlewares.size()) {
            // 모든 미들웨어 실행 완료 -> 최종 핸들러 실행
            finalHandler.handle(exchange);
        } else {
            // 현재 미들웨어 실행
            Middleware current = middlewares.get(index);
            current.handle(exchange, () -> {
                try {
                    executeChain(exchange, index + 1, finalHandler);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
