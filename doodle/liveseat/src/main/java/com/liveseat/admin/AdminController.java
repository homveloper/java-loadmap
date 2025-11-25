package com.liveseat.admin;

import com.liveseat.booking.BookingService;
import com.liveseat.concert.ConcertDto.*;
import com.liveseat.concert.ConcertService;
import com.liveseat.common.Result;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Admin MVC Controller - 관리자 기능
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ConcertService concertService;
    private final BookingService bookingService;

    public AdminController(ConcertService concertService, BookingService bookingService) {
        this.concertService = concertService;
        this.bookingService = bookingService;
    }

    /**
     * 관리자 메인 대시보드
     * GET /admin
     */
    @GetMapping
    public String dashboard(Model model) {
        var concerts = concertService.getAvailableConcerts();
        model.addAttribute("concerts", concerts);
        return "admin/dashboard";
    }

    /**
     * 공연 등록 폼
     * GET /admin/concerts/new
     */
    @GetMapping("/concerts/new")
    public String registerForm(Model model) {
        model.addAttribute("concertRequest", new CreateConcertRequest(
                "", "", null, null, 0, 0, 0, 0));
        return "admin/register";
    }

    /**
     * 공연 등록 처리
     * POST /admin/concerts
     */
    @PostMapping("/concerts")
    public String registerConcert(
            @Valid @ModelAttribute("concertRequest") CreateConcertRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/register";
        }

        Result<ConcertDetailResponse, ConcertService.ConcertError> result =
                concertService.createConcert(request);

        return switch (result) {
            case Result.Success<ConcertDetailResponse, ConcertService.ConcertError> s ->
                    "redirect:/admin?registered=true";
            case Result.Failure<ConcertDetailResponse, ConcertService.ConcertError> f -> {
                String errorMessage = switch (f.error()) {
                    case ConcertService.ConcertError.InvalidDateRange e ->
                            "종료 날짜는 시작 날짜 이후여야 합니다.";
                    default -> "공연 등록 중 오류가 발생했습니다.";
                };
                model.addAttribute("errorMessage", errorMessage);
                yield "admin/register";
            }
        };
    }

    /**
     * 특정 공연의 통계
     * GET /admin/concerts/{id}/stats
     */
    @GetMapping("/concerts/{id}/stats")
    public String concertStats(@PathVariable Long id, Model model) {
        var concertResult = concertService.getConcertById(id);
        var stats = bookingService.getConcertStats(id);

        return switch (concertResult) {
            case Result.Success<ConcertDetailResponse, ConcertService.ConcertError> s -> {
                model.addAttribute("concert", s.value());
                model.addAttribute("stats", stats);
                yield "admin/stats";
            }
            case Result.Failure<ConcertDetailResponse, ConcertService.ConcertError> f -> {
                model.addAttribute("errorMessage", "공연을 찾을 수 없습니다.");
                yield "error/404";
            }
        };
    }
}
