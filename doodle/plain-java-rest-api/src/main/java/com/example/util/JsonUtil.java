package com.example.util;

import com.example.model.Post;
import com.example.model.Product;
import com.example.model.User;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON 직렬화/역직렬화 유틸리티
 *
 * 외부 라이브러리 없이 수동으로 JSON을 처리합니다.
 * 실제 프로덕션에서는 Jackson, Gson 등을 사용하세요.
 */
public class JsonUtil {

    // ========== User JSON 변환 ==========

    /**
     * User 객체를 JSON 문자열로 변환
     */
    public static String toJson(User user) {
        if (user == null) {
            return "null";
        }
        return String.format(
                "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}",
                user.getId(),
                escapeJson(user.getName()),
                escapeJson(user.getEmail())
        );
    }

    /**
     * User 리스트를 JSON 배열로 변환
     */
    public static String toJson(List<User> users) {
        if (users == null || users.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            sb.append(toJson(users.get(i)));
            if (i < users.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * JSON 문자열을 User 객체로 변환
     */
    public static User userFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        User user = new User();

        // "name":"홍길동" 패턴 찾기
        Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher nameMatcher = namePattern.matcher(json);
        if (nameMatcher.find()) {
            user.setName(nameMatcher.group(1));
        }

        // "email":"hong@example.com" 패턴 찾기
        Pattern emailPattern = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"");
        Matcher emailMatcher = emailPattern.matcher(json);
        if (emailMatcher.find()) {
            user.setEmail(emailMatcher.group(1));
        }

        // "id":1 패턴 찾기 (선택적)
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            user.setId(Long.parseLong(idMatcher.group(1)));
        }

        return user;
    }

    // ========== Post JSON 변환 ==========

    /**
     * Post 객체를 JSON 문자열로 변환
     */
    public static String postToJson(Post post) {
        if (post == null) {
            return "null";
        }
        return String.format(
                "{\"id\":%d,\"title\":\"%s\",\"content\":\"%s\",\"authorId\":%d}",
                post.getId(),
                escapeJson(post.getTitle()),
                escapeJson(post.getContent()),
                post.getAuthorId()
        );
    }

    /**
     * Post 리스트를 JSON 배열로 변환
     */
    public static String postsToJson(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < posts.size(); i++) {
            sb.append(postToJson(posts.get(i)));
            if (i < posts.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * JSON 문자열을 Post 객체로 변환
     */
    public static Post postFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        Post post = new Post();

        // "title":"제목" 패턴 찾기
        Pattern titlePattern = Pattern.compile("\"title\"\\s*:\\s*\"([^\"]+)\"");
        Matcher titleMatcher = titlePattern.matcher(json);
        if (titleMatcher.find()) {
            post.setTitle(titleMatcher.group(1));
        }

        // "content":"내용" 패턴 찾기
        Pattern contentPattern = Pattern.compile("\"content\"\\s*:\\s*\"([^\"]+)\"");
        Matcher contentMatcher = contentPattern.matcher(json);
        if (contentMatcher.find()) {
            post.setContent(contentMatcher.group(1));
        }

        // "authorId":1 패턴 찾기
        Pattern authorIdPattern = Pattern.compile("\"authorId\"\\s*:\\s*(\\d+)");
        Matcher authorIdMatcher = authorIdPattern.matcher(json);
        if (authorIdMatcher.find()) {
            post.setAuthorId(Long.parseLong(authorIdMatcher.group(1)));
        }

        // "id":1 패턴 찾기 (선택적)
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            post.setId(Long.parseLong(idMatcher.group(1)));
        }

        return post;
    }

    // ========== Product JSON 변환 ==========

    /**
     * Product 객체를 JSON 문자열로 변환
     */
    public static String productToJson(Product product) {
        if (product == null) {
            return "null";
        }
        return String.format(
                "{\"id\":%d,\"name\":\"%s\",\"price\":%d,\"stock\":%d}",
                product.getId(),
                escapeJson(product.getName()),
                product.getPrice(),
                product.getStock()
        );
    }

    /**
     * Product 리스트를 JSON 배열로 변환
     */
    public static String productsToJson(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            sb.append(productToJson(products.get(i)));
            if (i < products.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * JSON 문자열을 Product 객체로 변환
     */
    public static Product productFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        Product product = new Product();

        // "name":"상품명" 패턴 찾기
        Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher nameMatcher = namePattern.matcher(json);
        if (nameMatcher.find()) {
            product.setName(nameMatcher.group(1));
        }

        // "price":10000 패턴 찾기
        Pattern pricePattern = Pattern.compile("\"price\"\\s*:\\s*(\\d+)");
        Matcher priceMatcher = pricePattern.matcher(json);
        if (priceMatcher.find()) {
            product.setPrice(Integer.parseInt(priceMatcher.group(1)));
        }

        // "stock":100 패턴 찾기
        Pattern stockPattern = Pattern.compile("\"stock\"\\s*:\\s*(\\d+)");
        Matcher stockMatcher = stockPattern.matcher(json);
        if (stockMatcher.find()) {
            product.setStock(Integer.parseInt(stockMatcher.group(1)));
        }

        // "id":1 패턴 찾기 (선택적)
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            product.setId(Long.parseLong(idMatcher.group(1)));
        }

        return product;
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * JSON 문자열 이스케이프 처리
     */
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 에러 응답 JSON 생성
     */
    public static String errorJson(String message) {
        return String.format("{\"error\":\"%s\"}", escapeJson(message));
    }

    /**
     * 성공 메시지 JSON 생성
     */
    public static String messageJson(String message) {
        return String.format("{\"message\":\"%s\"}", escapeJson(message));
    }
}
