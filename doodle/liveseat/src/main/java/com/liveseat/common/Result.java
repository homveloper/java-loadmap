package com.liveseat.common;

import java.util.Optional;
import java.util.function.Function;

/**
 * Result Pattern: Golang-inspired error handling
 * 예외 던지기 대신 Result<T, E>로 성공/실패를 명시적으로 표현
 *
 * @param <T> 성공 시 반환 값 타입
 * @param <E> 실패 시 에러 타입
 */
public sealed interface Result<T, E> {

    /**
     * 성공 케이스
     */
    record Success<T, E>(T value) implements Result<T, E> {
        public Success {
            if (value == null) {
                throw new IllegalArgumentException("Success value cannot be null");
            }
        }
    }

    /**
     * 실패 케이스
     */
    record Failure<T, E>(E error) implements Result<T, E> {
        public Failure {
            if (error == null) {
                throw new IllegalArgumentException("Failure error cannot be null");
            }
        }
    }

    // Factory methods
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    // Helper methods
    default boolean isSuccess() {
        return this instanceof Success<T, E>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T, E>;
    }

    default Optional<T> getValue() {
        return switch (this) {
            case Success<T, E> s -> Optional.of(s.value());
            case Failure<T, E> f -> Optional.empty();
        };
    }

    default Optional<E> getError() {
        return switch (this) {
            case Success<T, E> s -> Optional.empty();
            case Failure<T, E> f -> Optional.of(f.error());
        };
    }

    // Map 성공 값을 변환
    default <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
        return switch (this) {
            case Success<T, E> s -> Result.success(mapper.apply(s.value()));
            case Failure<T, E> f -> Result.failure(f.error());
        };
    }

    // FlatMap for chaining operations
    default <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
        return switch (this) {
            case Success<T, E> s -> mapper.apply(s.value());
            case Failure<T, E> f -> Result.failure(f.error());
        };
    }
}
