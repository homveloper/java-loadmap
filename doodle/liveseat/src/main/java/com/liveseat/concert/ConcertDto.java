package com.liveseat.concert;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Concert DTO - Modern Java Record 사용
 */
public class ConcertDto {

    /**
     * 공연 목록 조회 응답
     */
    public record ConcertListResponse(
            Long id,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            Integer availableSeats,
            Integer totalSeats
    ) {
        public static ConcertListResponse from(Concert concert) {
            return new ConcertListResponse(
                    concert.getId(),
                    concert.getTitle(),
                    concert.getStartDate(),
                    concert.getEndDate(),
                    concert.getAvailableSeats(),
                    concert.getTotalSeats()
            );
        }
    }

    /**
     * 공연 상세 조회 응답
     */
    public record ConcertDetailResponse(
            Long id,
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Integer priceVip,
            Integer priceR,
            Integer priceS,
            Integer availableSeats,
            Integer totalSeats,
            LocalDateTime createdAt
    ) {
        public static ConcertDetailResponse from(Concert concert) {
            return new ConcertDetailResponse(
                    concert.getId(),
                    concert.getTitle(),
                    concert.getDescription(),
                    concert.getStartDate(),
                    concert.getEndDate(),
                    concert.getPriceVip(),
                    concert.getPriceR(),
                    concert.getPriceS(),
                    concert.getAvailableSeats(),
                    concert.getTotalSeats(),
                    concert.getCreatedAt()
            );
        }
    }

    /**
     * 공연 등록 요청
     */
    public record CreateConcertRequest(
            @NotBlank(message = "제목은 필수입니다")
            @Size(max = 200, message = "제목은 200자 이하여야 합니다")
            String title,

            @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
            String description,

            @NotNull(message = "시작 날짜는 필수입니다")
            @Future(message = "시작 날짜는 미래여야 합니다")
            LocalDate startDate,

            @NotNull(message = "종료 날짜는 필수입니다")
            @Future(message = "종료 날짜는 미래여야 합니다")
            LocalDate endDate,

            @NotNull(message = "VIP 가격은 필수입니다")
            @Min(value = 0, message = "가격은 0 이상이어야 합니다")
            Integer priceVip,

            @NotNull(message = "R석 가격은 필수입니다")
            @Min(value = 0, message = "가격은 0 이상이어야 합니다")
            Integer priceR,

            @NotNull(message = "S석 가격은 필수입니다")
            @Min(value = 0, message = "가격은 0 이상이어야 합니다")
            Integer priceS,

            @NotNull(message = "총 좌석 수는 필수입니다")
            @Min(value = 1, message = "좌석 수는 최소 1개 이상이어야 합니다")
            Integer totalSeats
    ) {
        public Concert toEntity() {
            return new Concert(
                    title,
                    description,
                    startDate,
                    endDate,
                    priceVip,
                    priceR,
                    priceS,
                    totalSeats
            );
        }
    }
}
