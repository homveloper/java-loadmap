package com.liveseat.concert;

import com.liveseat.common.Result;
import com.liveseat.concert.ConcertDto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Concert REST API Controller - JSON 응답
 */
@RestController
@RequestMapping("/api/concerts")
@Tag(name = "Concert", description = "공연 관리 API")
public class ConcertRestController {

    private final ConcertService concertService;

    public ConcertRestController(ConcertService concertService) {
        this.concertService = concertService;
    }

    /**
     * 공연 목록 조회
     */
    @GetMapping
    @Operation(summary = "공연 목록 조회", description = "페이징된 공연 목록을 반환합니다")
    public ResponseEntity<Page<ConcertListResponse>> getConcerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConcertListResponse> concerts = concertService.getConcerts(pageable);
        return ResponseEntity.ok(concerts);
    }

    /**
     * 공연 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "공연 상세 조회", description = "특정 공연의 상세 정보를 반환합니다")
    public ResponseEntity<?> getConcert(@PathVariable Long id) {
        Result<ConcertDetailResponse, ConcertService.ConcertError> result =
                concertService.getConcertById(id);

        return switch (result) {
            case Result.Success<ConcertDetailResponse, ConcertService.ConcertError> s ->
                    ResponseEntity.ok(s.value());
            case Result.Failure<ConcertDetailResponse, ConcertService.ConcertError> f ->
                switch (f.error()) {
                    case ConcertService.ConcertError.NotFound e ->
                            ResponseEntity.notFound().build();
                    default -> ResponseEntity.internalServerError().build();
                };
        };
    }

    /**
     * 예매 가능한 공연 조회
     */
    @GetMapping("/available")
    @Operation(summary = "예매 가능한 공연 조회", description = "좌석이 남아있는 공연 목록을 반환합니다")
    public ResponseEntity<List<ConcertListResponse>> getAvailableConcerts() {
        List<ConcertListResponse> concerts = concertService.getAvailableConcerts();
        return ResponseEntity.ok(concerts);
    }

    /**
     * 공연 검색
     */
    @GetMapping("/search")
    @Operation(summary = "공연 검색", description = "제목으로 공연을 검색합니다")
    public ResponseEntity<Page<ConcertListResponse>> searchConcerts(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConcertListResponse> concerts = concertService.searchConcerts(title, pageable);
        return ResponseEntity.ok(concerts);
    }
}
