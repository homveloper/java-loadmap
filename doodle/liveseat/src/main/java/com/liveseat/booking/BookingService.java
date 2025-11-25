package com.liveseat.booking;

import com.liveseat.booking.Booking.BookingStatus;
import com.liveseat.booking.BookingDto.*;
import com.liveseat.common.Result;
import com.liveseat.concert.Concert;
import com.liveseat.concert.ConcertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Booking Service - Result Pattern + Transactional 동시성 제어
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ConcertRepository concertRepository;

    public BookingService(BookingRepository bookingRepository, ConcertRepository concertRepository) {
        this.bookingRepository = bookingRepository;
        this.concertRepository = concertRepository;
    }

    /**
     * 예매 생성 - 동시성 제어 (Optimistic Locking)
     */
    @Transactional
    public Result<BookingResponse, BookingError> createBooking(CreateBookingRequest request) {
        // 1. 공연 조회
        Concert concert = concertRepository.findById(request.concertId())
                .orElse(null);
        if (concert == null) {
            return Result.failure(new BookingError.ConcertNotFound(request.concertId()));
        }

        // 2. 날짜 검증
        if (request.bookingDate().isBefore(concert.getStartDate()) ||
            request.bookingDate().isAfter(concert.getEndDate())) {
            return Result.failure(new BookingError.InvalidDate(
                    request.bookingDate(), concert.getStartDate(), concert.getEndDate()));
        }

        // 3. 좌석 예약 시도 (동시성 제어)
        boolean reserved = concert.reserveSeats(request.seatCount());
        if (!reserved) {
            return Result.failure(new BookingError.SoldOut(
                    concert.getId(), request.seatCount(), concert.getAvailableSeats()));
        }

        // 4. 예매 생성
        Booking booking = new Booking(
                concert,
                request.customerName(),
                request.customerEmail(),
                request.bookingDate(),
                request.seatGrade(),
                request.seatCount()
        );

        Booking saved = bookingRepository.save(booking);
        concertRepository.save(concert); // Concert 엔티티 업데이트 (좌석 감소)

        return Result.success(BookingResponse.from(saved));
    }

    /**
     * 내 예매 내역 조회
     */
    public List<BookingListResponse> getMyBookings(String email) {
        return bookingRepository.findByCustomerEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(BookingListResponse::from)
                .toList();
    }

    /**
     * 예매 상세 조회
     */
    public Result<BookingResponse, BookingError> getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(BookingResponse::from)
                .<Result<BookingResponse, BookingError>>map(Result::success)
                .orElse(Result.failure(new BookingError.BookingNotFound(id)));
    }

    /**
     * 예매 취소
     */
    @Transactional
    public Result<BookingResponse, BookingError> cancelBooking(Long id, String email) {
        // 1. 예매 조회
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            return Result.failure(new BookingError.BookingNotFound(id));
        }

        // 2. 이메일 확인 (본인 확인)
        if (!booking.getCustomerEmail().equals(email)) {
            return Result.failure(new BookingError.Unauthorized(id, email));
        }

        // 3. 이미 취소된 예매 확인
        if (booking.isCancelled()) {
            return Result.failure(new BookingError.AlreadyCancelled(id));
        }

        // 4. 취소 처리 (Soft Delete)
        booking.cancel();
        Concert concert = booking.getConcert();
        concert.cancelSeats(booking.getSeatCount());

        bookingRepository.save(booking);
        concertRepository.save(concert);

        return Result.success(BookingResponse.from(booking));
    }

    /**
     * 특정 공연의 통계
     */
    public ConcertBookingStats getConcertStats(Long concertId) {
        Long totalBookings = bookingRepository.countByConcertIdAndStatus(
                concertId, BookingStatus.CONFIRMED);
        Integer totalSeats = bookingRepository.sumSeatCountByConcertIdAndStatus(
                concertId, BookingStatus.CONFIRMED);

        return new ConcertBookingStats(
                concertId,
                totalBookings != null ? totalBookings : 0L,
                totalSeats != null ? totalSeats : 0
        );
    }

    public record ConcertBookingStats(Long concertId, Long totalBookings, Integer totalSeatsBooked) {}

    /**
     * Booking 에러 타입 (Sealed Interface)
     */
    public sealed interface BookingError {
        record ConcertNotFound(Long concertId) implements BookingError {}
        record BookingNotFound(Long bookingId) implements BookingError {}
        record InvalidDate(LocalDate requested, LocalDate start, LocalDate end) implements BookingError {}
        record SoldOut(Long concertId, Integer requested, Integer available) implements BookingError {}
        record Unauthorized(Long bookingId, String email) implements BookingError {}
        record AlreadyCancelled(Long bookingId) implements BookingError {}
    }
}
