package practical.guide.examples;

import java.util.function.Function;

/**
 * Result 패턴 구현 - Error as Value
 *
 * Go의 (value, error) 패턴을 Java에서 구현한 예시
 * 예외 대신 Result 타입으로 에러를 명시적으로 처리
 */
public sealed interface Result<T> {

    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(Error error) implements Result<T> {}

    // 생성 메서드
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(Error error) {
        return new Failure<>(error);
    }

    // 상태 체크
    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    // 값 추출
    default T getValue() {
        return switch (this) {
            case Success<T> s -> s.value();
            case Failure<T> f -> throw new IllegalStateException("No value in failure");
        };
    }

    default Error getError() {
        return switch (this) {
            case Success<T> s -> throw new IllegalStateException("No error in success");
            case Failure<T> f -> f.error();
        };
    }

    // 함수형 메서드
    default <U> Result<U> map(Function<T, U> mapper) {
        return switch (this) {
            case Success<T> s -> success(mapper.apply(s.value()));
            case Failure<T> f -> failure(f.error());
        };
    }

    default <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        return switch (this) {
            case Success<T> s -> mapper.apply(s.value());
            case Failure<T> f -> failure(f.error());
        };
    }

    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T> s -> s.value();
            case Failure<T> f -> defaultValue;
        };
    }
}
