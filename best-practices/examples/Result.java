package com.example.common;

import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Result 타입 - Error as Value 패턴의 핵심
 *
 * Golang의 (value, error) 반환 패턴을 Java로 구현
 * 예측 가능한 비즈니스 에러를 명시적으로 처리
 *
 * 사용 예:
 * <pre>
 * Result&lt;User, UserError&gt; result = userService.getUserById(id);
 *
 * return switch (result) {
 *     case Result.Success&lt;User, UserError&gt; s -&gt; handleSuccess(s.value());
 *     case Result.Failure&lt;User, UserError&gt; f -&gt; handleError(f.error());
 * };
 * </pre>
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    /**
     * Success case - 성공 결과를 포함
     */
    record Success<T, E>(T value) implements Result<T, E> {
        public Success {
            // null 체크는 선택적 (Void 타입의 경우 null 허용)
        }
    }

    /**
     * Failure case - 에러를 포함
     */
    record Failure<T, E>(E error) implements Result<T, E> {
        public Failure {
            if (error == null) {
                throw new IllegalArgumentException("Error cannot be null");
            }
        }
    }

    // =========================================================================
    // Factory Methods
    // =========================================================================

    /**
     * Success 결과 생성
     */
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    /**
     * Failure 결과 생성
     */
    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    // =========================================================================
    // Query Methods
    // =========================================================================

    /**
     * 성공 여부 확인
     */
    default boolean isSuccess() {
        return this instanceof Success<T, E>;
    }

    /**
     * 실패 여부 확인
     */
    default boolean isFailure() {
        return this instanceof Failure<T, E>;
    }

    // =========================================================================
    // Value Extraction
    // =========================================================================

    /**
     * 성공 값 추출 (실패 시 예외 발생)
     *
     * 주의: 프로덕션 코드에서는 패턴 매칭을 권장
     */
    default T getOrThrow() {
        return switch (this) {
            case Success<T, E> s -> s.value();
            case Failure<T, E> f -> throw new IllegalStateException(
                "Cannot extract value from failure: " + f.error()
            );
        };
    }

    /**
     * 성공 값 추출 (실패 시 기본값 반환)
     */
    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T, E> s -> s.value();
            case Failure<T, E> f -> defaultValue;
        };
    }

    /**
     * 성공 값 추출 (실패 시 Supplier로부터 값 생성)
     */
    default T getOrElse(Supplier<T> supplier) {
        return switch (this) {
            case Success<T, E> s -> s.value();
            case Failure<T, E> f -> supplier.get();
        };
    }

    /**
     * 성공 값을 Optional로 변환
     */
    default java.util.Optional<T> toOptional() {
        return switch (this) {
            case Success<T, E> s -> java.util.Optional.ofNullable(s.value());
            case Failure<T, E> f -> java.util.Optional.empty();
        };
    }

    // =========================================================================
    // Transformation Methods (Functor)
    // =========================================================================

    /**
     * 성공 값을 변환 (실패는 그대로 전파)
     *
     * 예: Result&lt;User, Error&gt; -&gt; Result&lt;UserDto, Error&gt;
     */
    default <U> Result<U, E> map(Function<T, U> mapper) {
        return switch (this) {
            case Success<T, E> s -> Result.success(mapper.apply(s.value()));
            case Failure<T, E> f -> Result.failure(f.error());
        };
    }

    /**
     * 에러를 변환 (성공은 그대로 전파)
     */
    default <F> Result<T, F> mapError(Function<E, F> mapper) {
        return switch (this) {
            case Success<T, E> s -> Result.success(s.value());
            case Failure<T, E> f -> Result.failure(mapper.apply(f.error()));
        };
    }

    // =========================================================================
    // Chaining Methods (Monad)
    // =========================================================================

    /**
     * Result를 반환하는 함수를 체이닝
     *
     * flatMap은 여러 Result 연산을 순차적으로 연결할 때 사용
     *
     * 예:
     * <pre>
     * return getUserById(id)
     *     .flatMap(user -&gt; validateUser(user))
     *     .flatMap(user -&gt; updateUser(user))
     *     .flatMap(user -&gt; sendEmail(user));
     * </pre>
     */
    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        return switch (this) {
            case Success<T, E> s -> mapper.apply(s.value());
            case Failure<T, E> f -> Result.failure(f.error());
        };
    }

    /**
     * 실패 시 복구 시도
     */
    default Result<T, E> recover(Function<E, Result<T, E>> recovery) {
        return switch (this) {
            case Success<T, E> s -> this;
            case Failure<T, E> f -> recovery.apply(f.error());
        };
    }

    // =========================================================================
    // Side Effects
    // =========================================================================

    /**
     * 성공 시 부수 효과 실행
     */
    default Result<T, E> onSuccess(Consumer<T> action) {
        if (this instanceof Success<T, E> s) {
            action.accept(s.value());
        }
        return this;
    }

    /**
     * 실패 시 부수 효과 실행
     */
    default Result<T, E> onFailure(Consumer<E> action) {
        if (this instanceof Failure<T, E> f) {
            action.accept(f.error());
        }
        return this;
    }

    /**
     * 성공/실패 양쪽에 대한 부수 효과 실행
     */
    default Result<T, E> peek(Consumer<T> onSuccess, Consumer<E> onFailure) {
        return switch (this) {
            case Success<T, E> s -> {
                onSuccess.accept(s.value());
                yield this;
            }
            case Failure<T, E> f -> {
                onFailure.accept(f.error());
                yield this;
            }
        };
    }

    // =========================================================================
    // Combining Results
    // =========================================================================

    /**
     * 두 Result를 결합 (둘 다 성공해야 성공)
     */
    default <U, R> Result<R, E> combine(
        Result<U, E> other,
        java.util.function.BiFunction<T, U, R> combiner
    ) {
        return switch (this) {
            case Success<T, E> s1 -> switch (other) {
                case Success<U, E> s2 -> Result.success(combiner.apply(s1.value(), s2.value()));
                case Failure<U, E> f2 -> Result.failure(f2.error());
            };
            case Failure<T, E> f1 -> Result.failure(f1.error());
        };
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    /**
     * Void 타입의 성공 결과 생성 (부수 효과만 있는 작업용)
     */
    static <E> Result<Void, E> successVoid() {
        return success(null);
    }

    /**
     * Exception을 Result로 변환
     */
    static <T, E> Result<T, E> of(
        Supplier<T> supplier,
        Function<Exception, E> errorMapper
    ) {
        try {
            return success(supplier.get());
        } catch (Exception e) {
            return failure(errorMapper.apply(e));
        }
    }

    /**
     * 여러 Result를 모아서 하나의 List Result로 변환
     * 하나라도 실패하면 첫 번째 실패를 반환
     */
    static <T, E> Result<java.util.List<T>, E> sequence(java.util.List<Result<T, E>> results) {
        java.util.List<T> values = new java.util.ArrayList<>();

        for (Result<T, E> result : results) {
            switch (result) {
                case Success<T, E> s -> values.add(s.value());
                case Failure<T, E> f -> {
                    return Result.failure(f.error());
                }
            }
        }

        return Result.success(values);
    }
}
