package com.liveseat.concert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long> {

    /**
     * 현재 예매 가능한 공연 조회 (종료일이 오늘 이후)
     */
    Page<Concert> findByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate today, Pageable pageable);

    /**
     * 좌석이 남아있는 공연만 조회
     */
    @Query("SELECT c FROM Concert c WHERE c.availableSeats > 0 AND c.endDate >= :today ORDER BY c.startDate ASC")
    List<Concert> findAvailableConcerts(LocalDate today);

    /**
     * 제목으로 검색
     */
    Page<Concert> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
