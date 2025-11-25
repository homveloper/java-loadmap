package com.liveseat.concert;

import com.liveseat.common.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Concert MVC Controller - Thymeleaf 뷰 반환
 */
@Controller
@RequestMapping("/concerts")
public class ConcertController {

    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    /**
     * 공연 목록 페이지
     * GET /concerts?page=0&size=10
     */
    @GetMapping
    public String listConcerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConcertDto.ConcertListResponse> concerts = concertService.getConcerts(pageable);

        model.addAttribute("concerts", concerts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", concerts.getTotalPages());

        return "concert/list";
    }

    /**
     * 공연 상세 페이지
     * GET /concerts/{id}
     */
    @GetMapping("/{id}")
    public String concertDetail(@PathVariable Long id, Model model) {
        Result<ConcertDto.ConcertDetailResponse, ConcertService.ConcertError> result =
                concertService.getConcertById(id);

        return switch (result) {
            case Result.Success<ConcertDto.ConcertDetailResponse, ConcertService.ConcertError> s -> {
                model.addAttribute("concert", s.value());
                yield "concert/detail";
            }
            case Result.Failure<ConcertDto.ConcertDetailResponse, ConcertService.ConcertError> f ->
                switch (f.error()) {
                    case ConcertService.ConcertError.NotFound e -> {
                        model.addAttribute("errorMessage", "공연을 찾을 수 없습니다: ID " + e.id());
                        yield "error/404";
                    }
                    default -> "error/500";
                };
        };
    }
}
