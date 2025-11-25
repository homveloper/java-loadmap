package com.liveseat.booking;

import com.liveseat.booking.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * 특정 이메일의 예매 내역 조회
     */
    List<Booking> findByCustomerEmailOrderByCreatedAtDesc(String email);

    /**
     * 특정 공연의 전체 예매 수 (취소 제외)
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.concert.id = :concertId AND b.status = :status")
    Long countByConcertIdAndStatus(Long concertId, BookingStatus status);

    /**
     * 특정 공연의 총 예매 좌석 수 (취소 제외)
     */
    @Query("SELECT SUM(b.seatCount) FROM Booking b WHERE b.concert.id = :concertId AND b.status = :status")
    Integer sumSeatCountByConcertIdAndStatus(Long concertId, BookingStatus status);
}
