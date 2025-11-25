package com.liveseat.admin;

import com.liveseat.booking.BookingService;
import com.liveseat.concert.ConcertDto.*;
import com.liveseat.concert.ConcertService;
import com.liveseat.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin REST API Controller
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "관리자 API")
public class AdminRestController {

    private final ConcertService concertService;
    private final BookingService bookingService;

    public AdminRestController(ConcertService concertService, BookingService bookingService) {
        this.concertService = concertService;
        this.bookingService = bookingService;
    }

    /**
     * 공연 등록
     */
    @PostMapping("/concerts")
    @Operation(summary = "공연 등록", description = "새로운 공연을 등록합니다")
    public ResponseEntity<?> createConcert(@Valid @RequestBody CreateConcertRequest request) {
        Result<ConcertDetailResponse, ConcertService.ConcertError> result =
                concertService.createConcert(request);

        return switch (result) {
            case Result.Success<ConcertDetailResponse, ConcertService.ConcertError> s ->
                    ResponseEntity.status(HttpStatus.CREATED).body(s.value());
            case Result.Failure<ConcertDetailResponse, ConcertService.ConcertError> f ->
                    switch (f.error()) {
                        case ConcertService.ConcertError.InvalidDateRange e ->
                                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of("error", "Invalid date range",
                                                "startDate", e.startDate(),
                                                "endDate", e.endDate()));
                        default -> ResponseEntity.internalServerError()
                                .body(Map.of("error", "Internal server error"));
                    };
        };
    }

    /**
     * 공연 통계 조회
     */
    @GetMapping("/concerts/{id}/stats")
    @Operation(summary = "공연 통계 조회", description = "특정 공연의 예매 통계를 조회합니다")
    public ResponseEntity<?> getConcertStats(@PathVariable Long id) {
        var stats = bookingService.getConcertStats(id);
        return ResponseEntity.ok(stats);
    }
}
