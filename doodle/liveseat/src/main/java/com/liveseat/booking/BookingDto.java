package com.liveseat.booking;

import com.liveseat.booking.Booking.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Booking DTO - Record 사용
 */
public class BookingDto {

    /**
     * 예매 요청
     */
    public record CreateBookingRequest(
            @NotNull(message = "공연 ID는 필수입니다")
            Long concertId,

            @NotBlank(message = "예매자 이름은 필수입니다")
            @Size(max = 100, message = "이름은 100자 이하여야 합니다")
            String customerName,

            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "올바른 이메일 형식이어야 합니다")
            String customerEmail,

            @NotNull(message = "예매 날짜는 필수입니다")
            @Future(message = "예매 날짜는 미래여야 합니다")
            LocalDate bookingDate,

            @NotNull(message = "좌석 등급은 필수입니다")
            SeatGrade seatGrade,

            @NotNull(message = "좌석 수는 필수입니다")
            @Min(value = 1, message = "최소 1개 이상 예매해야 합니다")
            @Max(value = 10, message = "최대 10개까지 예매 가능합니다")
            Integer seatCount
    ) {}

    /**
     * 예매 응답
     */
    public record BookingResponse(
            Long id,
            Long concertId,
            String concertTitle,
            String customerName,
            String customerEmail,
            LocalDate bookingDate,
            SeatGrade seatGrade,
            Integer seatCount,
            Integer totalPrice,
            BookingStatus status,
            LocalDateTime createdAt
    ) {
        public static BookingResponse from(Booking booking) {
            return new BookingResponse(
                    booking.getId(),
                    booking.getConcert().getId(),
                    booking.getConcert().getTitle(),
                    booking.getCustomerName(),
                    booking.getCustomerEmail(),
                    booking.getBookingDate(),
                    booking.getSeatGrade(),
                    booking.getSeatCount(),
                    booking.getTotalPrice(),
                    booking.getStatus(),
                    booking.getCreatedAt()
            );
        }
    }

    /**
     * 예매 내역 조회 응답 (간단 버전)
     */
    public record BookingListResponse(
            Long id,
            String concertTitle,
            LocalDate bookingDate,
            SeatGrade seatGrade,
            Integer seatCount,
            Integer totalPrice,
            BookingStatus status
    ) {
        public static BookingListResponse from(Booking booking) {
            return new BookingListResponse(
                    booking.getId(),
                    booking.getConcert().getTitle(),
                    booking.getBookingDate(),
                    booking.getSeatGrade(),
                    booking.getSeatCount(),
                    booking.getTotalPrice(),
                    booking.getStatus()
            );
        }
    }
}
