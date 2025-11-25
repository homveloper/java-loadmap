package com.liveseat.concert;

import com.liveseat.common.Result;
import com.liveseat.concert.ConcertDto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Concert Service - Result Pattern 사용
 */
@Service
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    /**
     * 공연 목록 조회 (페이징)
     */
    public Page<ConcertListResponse> getConcerts(Pageable pageable) {
        LocalDate today = LocalDate.now();
        return concertRepository
                .findByEndDateGreaterThanEqualOrderByStartDateAsc(today, pageable)
                .map(ConcertListResponse::from);
    }

    /**
     * 공연 상세 조회
     */
    public Result<ConcertDetailResponse, ConcertError> getConcertById(Long id) {
        return concertRepository.findById(id)
                .map(ConcertDetailResponse::from)
                .<Result<ConcertDetailResponse, ConcertError>>map(Result::success)
                .orElse(Result.failure(new ConcertError.NotFound(id)));
    }

    /**
     * 공연 등록
     */
    @Transactional
    public Result<ConcertDetailResponse, ConcertError> createConcert(CreateConcertRequest request) {
        // 날짜 검증
        if (request.endDate().isBefore(request.startDate())) {
            return Result.failure(new ConcertError.InvalidDateRange(
                    request.startDate(), request.endDate()));
        }

        Concert concert = request.toEntity();
        Concert saved = concertRepository.save(concert);
        return Result.success(ConcertDetailResponse.from(saved));
    }

    /**
     * 예매 가능한 공연 조회
     */
    public List<ConcertListResponse> getAvailableConcerts() {
        LocalDate today = LocalDate.now();
        return concertRepository.findAvailableConcerts(today)
                .stream()
                .map(ConcertListResponse::from)
                .toList();
    }

    /**
     * 제목으로 검색
     */
    public Page<ConcertListResponse> searchConcerts(String title, Pageable pageable) {
        return concertRepository
                .findByTitleContainingIgnoreCase(title, pageable)
                .map(ConcertListResponse::from);
    }

    /**
     * 공연 에러 타입 (Sealed Interface)
     */
    public sealed interface ConcertError {
        record NotFound(Long id) implements ConcertError {}
        record InvalidDateRange(LocalDate startDate, LocalDate endDate) implements ConcertError {}
    }
}
