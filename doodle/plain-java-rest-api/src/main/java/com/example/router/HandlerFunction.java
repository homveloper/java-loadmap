package com.example.router;

import java.io.IOException;

/**
 * 핸들러 함수 인터페이스
 *
 * Golang의 gin.HandlerFunc와 유사한 함수형 인터페이스
 * Context를 받아서 요청을 처리합니다.
 *
 * 사용 예:
 * <pre>
 * HandlerFunction handler = ctx -> {
 *     User user = userService.getUser(ctx.pathParam("id"));
 *     ctx.json(user);
 * };
 * </pre>
 */
@FunctionalInterface
public interface HandlerFunction {
    /**
     * HTTP 요청을 처리하는 메서드
     *
     * @param ctx HTTP 요청/응답 컨텍스트
     * @throws IOException I/O 처리 중 발생할 수 있는 예외
     */
    void handle(Context ctx) throws IOException;
}
