package practical.guide.examples;

/**
 * 에러 정보를 담는 불변 객체
 */
public record Error(String code, String message) {

    public static Error of(String code, String message) {
        return new Error(code, message);
    }

    // 자주 사용하는 에러들
    public static Error validation(String message) {
        return new Error("VALIDATION_ERROR", message);
    }

    public static Error notFound(String message) {
        return new Error("NOT_FOUND", message);
    }

    public static Error unauthorized(String message) {
        return new Error("UNAUTHORIZED", message);
    }

    public static Error internalError(String message) {
        return new Error("INTERNAL_ERROR", message);
    }
}
