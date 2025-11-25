package com.liveseat.booking;

import com.liveseat.booking.BookingDto.*;
import com.liveseat.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Booking REST API Controller - JSON 응답
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking", description = "예매 관리 API")
public class BookingRestController {

    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * 예매 생성
     */
    @PostMapping
    @Operation(summary = "예매 생성", description = "새로운 예매를 생성합니다")
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        Result<BookingResponse, BookingService.BookingError> result =
                bookingService.createBooking(request);

        return switch (result) {
            case Result.Success<BookingResponse, BookingService.BookingError> s ->
                    ResponseEntity.status(HttpStatus.CREATED).body(s.value());
            case Result.Failure<BookingResponse, BookingService.BookingError> f ->
                    switch (f.error()) {
                        case BookingService.BookingError.ConcertNotFound e ->
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("error", "Concert not found", "concertId", e.concertId()));
                        case BookingService.BookingError.InvalidDate e ->
                                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of("error", "Invalid date",
                                                "requested", e.requested(),
                                                "start", e.start(),
                                                "end", e.end()));
                        case BookingService.BookingError.SoldOut e ->
                                ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("error", "Sold out",
                                                "requested", e.requested(),
                                                "available", e.available()));
                        default -> ResponseEntity.internalServerError()
                                .body(Map.of("error", "Internal server error"));
                    };
        };
    }

    /**
     * 내 예매 내역 조회
     */
    @GetMapping("/my")
    @Operation(summary = "내 예매 내역 조회", description = "이메일로 예매 내역을 조회합니다")
    public ResponseEntity<List<BookingListResponse>> getMyBookings(@RequestParam String email) {
        List<BookingListResponse> bookings = bookingService.getMyBookings(email);
        return ResponseEntity.ok(bookings);
    }

    /**
     * 예매 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "예매 상세 조회", description = "특정 예매의 상세 정보를 조회합니다")
    public ResponseEntity<?> getBooking(@PathVariable Long id) {
        Result<BookingResponse, BookingService.BookingError> result =
                bookingService.getBookingById(id);

        return switch (result) {
            case Result.Success<BookingResponse, BookingService.BookingError> s ->
                    ResponseEntity.ok(s.value());
            case Result.Failure<BookingResponse, BookingService.BookingError> f ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Booking not found", "id", id));
        };
    }

    /**
     * 예매 취소
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "예매 취소", description = "예매를 취소합니다 (Soft Delete)")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, @RequestParam String email) {
        Result<BookingResponse, BookingService.BookingError> result =
                bookingService.cancelBooking(id, email);

        return switch (result) {
            case Result.Success<BookingResponse, BookingService.BookingError> s ->
                    ResponseEntity.ok(s.value());
            case Result.Failure<BookingResponse, BookingService.BookingError> f ->
                    switch (f.error()) {
                        case BookingService.BookingError.BookingNotFound e ->
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("error", "Booking not found", "id", e.bookingId()));
                        case BookingService.BookingError.Unauthorized e ->
                                ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("error", "Unauthorized", "id", e.bookingId()));
                        case BookingService.BookingError.AlreadyCancelled e ->
                                ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("error", "Already cancelled", "id", e.bookingId()));
                        default -> ResponseEntity.internalServerError()
                                .body(Map.of("error", "Internal server error"));
                    };
        };
    }
}
