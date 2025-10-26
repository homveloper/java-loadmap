package com.example.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object 예제들
 *
 * Primitive Obsession을 피하고 도메인 개념을 명시적으로 표현
 *
 * Value Object 특징:
 * 1. 불변성 (Immutable)
 * 2. 값 동등성 (Value Equality)
 * 3. 자가 유효성 검증 (Self-validation)
 * 4. 도메인 개념 표현
 */

// ============================================================================
// Money Value Object
// ============================================================================

/**
 * 금액을 표현하는 Value Object
 *
 * 문제: double price - 정밀도 문제, 통화 정보 없음
 * 해결: Money 타입으로 금액과 통화를 함께 관리
 */
public record Money(BigDecimal amount, Currency currency) {

    // 상수
    public static final Money ZERO_KRW = Money.of(0, Currency.getInstance("KRW"));
    public static final Money ZERO_USD = Money.of(0, Currency.getInstance("USD"));

    // Compact constructor - 유효성 검증
    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        if (amount.scale() > currency.getDefaultFractionDigits()) {
            amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
        }
    }

    // Factory methods
    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money of(long amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money krw(double amount) {
        return Money.of(amount, Currency.getInstance("KRW"));
    }

    public static Money usd(double amount) {
        return Money.of(amount, Currency.getInstance("USD"));
    }

    // 산술 연산
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(double multiplier) {
        return new Money(
            this.amount.multiply(BigDecimal.valueOf(multiplier)),
            this.currency
        );
    }

    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    public Money divide(double divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new Money(
            this.amount.divide(BigDecimal.valueOf(divisor), RoundingMode.HALF_UP),
            this.currency
        );
    }

    // 비교 연산
    public boolean isGreaterThan(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // 유틸리티
    private void ensureSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Cannot operate on different currencies: %s and %s",
                    this.currency.getCurrencyCode(),
                    other.currency.getCurrencyCode())
            );
        }
    }

    public String getFormattedAmount() {
        return String.format("%s %,.2f",
            currency.getCurrencyCode(),
            amount.doubleValue());
    }

    @Override
    public String toString() {
        return getFormattedAmount();
    }
}

// ============================================================================
// Email Value Object
// ============================================================================

/**
 * 이메일 주소를 표현하는 Value Object
 *
 * 문제: String email - 유효성 검증 없음, 의미 불명확
 * 해결: Email 타입으로 유효한 이메일만 보장
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        value = value.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String getLocalPart() {
        return value.substring(0, value.indexOf('@'));
    }

    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }

    public boolean isCorporateEmail() {
        String domain = getDomain();
        return !domain.equals("gmail.com")
            && !domain.equals("yahoo.com")
            && !domain.equals("hotmail.com")
            && !domain.equals("naver.com");
    }

    @Override
    public String toString() {
        return value;
    }
}

// ============================================================================
// PhoneNumber Value Object
// ============================================================================

/**
 * 전화번호를 표현하는 Value Object
 *
 * 문제: String phoneNumber - 형식 불일치, 유효성 검증 없음
 * 해결: PhoneNumber 타입으로 일관된 형식 보장
 */
public record PhoneNumber(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    public PhoneNumber {
        Objects.requireNonNull(value, "Phone number cannot be null");

        // 하이픈, 공백 제거
        value = value.replaceAll("[\\s-]", "");

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Invalid phone number format. Expected 10-11 digits: " + value
            );
        }
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    /**
     * 010-1234-5678 형식으로 반환
     */
    public String getFormatted() {
        if (value.length() == 10) {
            return value.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        } else {
            return value.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        }
    }

    /**
     * 010********로 마스킹
     */
    public String getMasked() {
        if (value.length() == 10) {
            return value.substring(0, 3) + "***" + value.substring(6);
        } else {
            return value.substring(0, 3) + "****" + value.substring(7);
        }
    }

    @Override
    public String toString() {
        return getFormatted();
    }
}

// ============================================================================
// UserId Value Object
// ============================================================================

/**
 * 사용자 ID를 표현하는 Value Object
 *
 * 문제: Long userId 또는 String userId - 타입 안전성 부족
 * 해결: UserId 타입으로 명시적 표현
 */
public record UserId(Long value) {

    public UserId {
        Objects.requireNonNull(value, "User ID cannot be null");
        if (value <= 0) {
            throw new IllegalArgumentException("User ID must be positive: " + value);
        }
    }

    public static UserId of(Long value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

// ============================================================================
// OrderNumber Value Object
// ============================================================================

/**
 * 주문번호를 표현하는 Value Object
 *
 * 패턴: ORD-20240126-000001
 */
public record OrderNumber(String value) {

    private static final Pattern ORDER_NUMBER_PATTERN =
        Pattern.compile("^ORD-\\d{8}-\\d{6}$");

    public OrderNumber {
        Objects.requireNonNull(value, "Order number cannot be null");
        value = value.toUpperCase();

        if (!ORDER_NUMBER_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid order number format: " + value);
        }
    }

    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }

    /**
     * 새로운 주문번호 생성
     * 형식: ORD-YYYYMMDD-XXXXXX
     */
    public static OrderNumber generate(int sequence) {
        String date = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String seqStr = String.format("%06d", sequence);
        return new OrderNumber(String.format("ORD-%s-%s", date, seqStr));
    }

    public String getDate() {
        return value.substring(4, 12);
    }

    public int getSequence() {
        return Integer.parseInt(value.substring(13));
    }

    @Override
    public String toString() {
        return value;
    }
}

// ============================================================================
// Percentage Value Object
// ============================================================================

/**
 * 백분율을 표현하는 Value Object
 *
 * 문제: double percentage - 0.1이 10%인지 0.1%인지 불명확
 * 해결: Percentage 타입으로 명시적 표현
 */
public record Percentage(BigDecimal value) {

    public Percentage {
        Objects.requireNonNull(value, "Percentage cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Percentage cannot be negative: " + value);
        }
        if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage cannot exceed 100: " + value);
        }
    }

    /**
     * 10% -> Percentage.of(10)
     */
    public static Percentage of(double value) {
        return new Percentage(BigDecimal.valueOf(value));
    }

    /**
     * 0.1 -> 10%
     */
    public static Percentage fromDecimal(double decimal) {
        return new Percentage(BigDecimal.valueOf(decimal * 100));
    }

    /**
     * 10% -> 0.1
     */
    public BigDecimal toDecimal() {
        return value.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    /**
     * Money에 퍼센티지 적용
     * 예: Money.krw(10000).applyPercentage(Percentage.of(10)) -> 1000원
     */
    public Money applyTo(Money money) {
        return money.multiply(toDecimal());
    }

    @Override
    public String toString() {
        return value + "%";
    }
}

// ============================================================================
// DateRange Value Object
// ============================================================================

/**
 * 날짜 범위를 표현하는 Value Object
 */
public record DateRange(java.time.LocalDate start, java.time.LocalDate end) {

    public DateRange {
        Objects.requireNonNull(start, "Start date cannot be null");
        Objects.requireNonNull(end, "End date cannot be null");

        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                "Start date must be before or equal to end date: " + start + " > " + end
            );
        }
    }

    public static DateRange of(java.time.LocalDate start, java.time.LocalDate end) {
        return new DateRange(start, end);
    }

    public static DateRange ofDays(java.time.LocalDate start, int days) {
        return new DateRange(start, start.plusDays(days));
    }

    public boolean contains(java.time.LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public boolean overlaps(DateRange other) {
        return !this.end.isBefore(other.start) && !other.end.isBefore(this.start);
    }

    public long getDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    }

    @Override
    public String toString() {
        return start + " ~ " + end;
    }
}

// ============================================================================
// 사용 예제
// ============================================================================

class ValueObjectUsageExample {

    // 안티패턴 ❌
    void badExample(Long userId, String email, double price, String currency) {
        // 타입 안전성 없음
        // 유효성 검증 누락 가능
        // 의미 불명확
    }

    // 베스트 프랙티스 ✅
    void goodExample(UserId userId, Email email, Money price) {
        // 타입 안전: userId와 email을 바꿔서 호출하면 컴파일 에러
        // 유효성 보장: 생성 시점에 이미 검증됨
        // 의미 명확: 각 파라미터의 역할이 명확함
    }

    // Value Object 연산 예제
    void calculations() {
        Money basePrice = Money.krw(10000);
        Percentage discountRate = Percentage.of(10);

        Money discountAmount = discountRate.applyTo(basePrice);  // 1000원
        Money finalPrice = basePrice.subtract(discountAmount);   // 9000원

        System.out.println("기본 가격: " + basePrice);           // KRW 10,000.00
        System.out.println("할인율: " + discountRate);          // 10%
        System.out.println("할인 금액: " + discountAmount);      // KRW 1,000.00
        System.out.println("최종 가격: " + finalPrice);          // KRW 9,000.00
    }
}
