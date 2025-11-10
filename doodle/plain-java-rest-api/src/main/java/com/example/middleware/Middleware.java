package com.example.middleware;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 * Middleware 인터페이스
 *
 * Golang의 미들웨어 패턴을 Java로 구현
 *
 * 사용 예:
 * <pre>
 * Middleware logger = (exchange, next) -> {
 *     System.out.println("Before");
 *     next.run();
 *     System.out.println("After");
 * };
 * </pre>
 */
@FunctionalInterface
public interface Middleware {
    /**
     * 미들웨어 로직 실행
     *
     * @param exchange HTTP 요청/응답 객체
     * @param next 다음 미들웨어 또는 최종 핸들러 호출
     * @throws IOException I/O 오류 발생 시
     */
    void handle(HttpExchange exchange, Runnable next) throws IOException;
}
