package com.liveseat.booking;

import com.liveseat.booking.BookingDto.*;
import com.liveseat.common.Result;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Booking MVC Controller - Thymeleaf 뷰 반환
 */
@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * 예매 폼 페이지
     * GET /bookings/new?concertId=1
     */
    @GetMapping("/new")
    public String bookingForm(@RequestParam Long concertId, Model model) {
        model.addAttribute("concertId", concertId);
        model.addAttribute("bookingRequest", new CreateBookingRequest(
                concertId, "", "", null, null, 1));
        return "booking/form";
    }

    /**
     * 예매 처리
     * POST /bookings
     */
    @PostMapping
    public String createBooking(
            @Valid @ModelAttribute("bookingRequest") CreateBookingRequest request,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("concertId", request.concertId());
            return "booking/form";
        }

        Result<BookingResponse, BookingService.BookingError> result =
                bookingService.createBooking(request);

        return switch (result) {
            case Result.Success<BookingResponse, BookingService.BookingError> s -> {
                // 세션에 이메일 저장 (간단한 인증)
                session.setAttribute("userEmail", s.value().customerEmail());
                model.addAttribute("booking", s.value());
                yield "booking/success";
            }
            case Result.Failure<BookingResponse, BookingService.BookingError> f -> {
                String errorMessage = switch (f.error()) {
                    case BookingService.BookingError.ConcertNotFound e ->
                            "공연을 찾을 수 없습니다.";
                    case BookingService.BookingError.InvalidDate e ->
                            "예매 날짜가 공연 기간을 벗어났습니다.";
                    case BookingService.BookingError.SoldOut e ->
                            String.format("죄송합니다. 잔여 좌석이 %d석밖에 없습니다.", e.available());
                    default -> "예매 처리 중 오류가 발생했습니다.";
                };
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("concertId", request.concertId());
                yield "booking/form";
            }
        };
    }

    /**
     * 내 예매 내역 페이지 (마이페이지)
     * GET /bookings/my
     */
    @GetMapping("/my")
    public String myBookings(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/bookings/login";
        }

        List<BookingListResponse> bookings = bookingService.getMyBookings(email);
        model.addAttribute("bookings", bookings);
        model.addAttribute("email", email);
        return "booking/mypage";
    }

    /**
     * 예매 취소
     * POST /bookings/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/bookings/login";
        }

        Result<BookingResponse, BookingService.BookingError> result =
                bookingService.cancelBooking(id, email);

        return switch (result) {
            case Result.Success<BookingResponse, BookingService.BookingError> s ->
                    "redirect:/bookings/my?cancelled=true";
            case Result.Failure<BookingResponse, BookingService.BookingError> f -> {
                String errorMessage = switch (f.error()) {
                    case BookingService.BookingError.BookingNotFound e ->
                            "예매 내역을 찾을 수 없습니다.";
                    case BookingService.BookingError.Unauthorized e ->
                            "본인의 예매만 취소할 수 있습니다.";
                    case BookingService.BookingError.AlreadyCancelled e ->
                            "이미 취소된 예매입니다.";
                    default -> "취소 처리 중 오류가 발생했습니다.";
                };
                yield "redirect:/bookings/my?error=" + errorMessage;
            }
        };
    }

    /**
     * 간단한 로그인 페이지 (세션 기반)
     */
    @GetMapping("/login")
    public String loginForm() {
        return "booking/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, HttpSession session) {
        session.setAttribute("userEmail", email);
        return "redirect:/bookings/my";
    }
}
