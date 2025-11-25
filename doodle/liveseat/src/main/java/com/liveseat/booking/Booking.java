package com.liveseat.booking;

import com.liveseat.concert.Concert;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 예매 Entity
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatGrade seatGrade;

    @Column(nullable = false)
    private Integer seatCount;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // JPA requires default constructor
    protected Booking() {}

    public Booking(Concert concert, String customerName, String customerEmail,
                   LocalDate bookingDate, SeatGrade seatGrade, Integer seatCount) {
        this.concert = concert;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.bookingDate = bookingDate;
        this.seatGrade = seatGrade;
        this.seatCount = seatCount;
        this.totalPrice = calculatePrice(concert, seatGrade, seatCount);
        this.status = BookingStatus.CONFIRMED;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic: 가격 계산
    private Integer calculatePrice(Concert concert, SeatGrade grade, Integer count) {
        int pricePerSeat = switch (grade) {
            case VIP -> concert.getPriceVip();
            case R -> concert.getPriceR();
            case S -> concert.getPriceS();
        };
        return pricePerSeat * count;
    }

    // Business logic: 예매 취소
    public void cancel() {
        if (this.status == BookingStatus.CONFIRMED) {
            this.status = BookingStatus.CANCELLED;
        }
    }

    public boolean isCancelled() {
        return this.status == BookingStatus.CANCELLED;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Concert getConcert() {
        return concert;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public SeatGrade getSeatGrade() {
        return seatGrade;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 좌석 등급
     */
    public enum SeatGrade {
        VIP, R, S
    }

    /**
     * 예매 상태
     */
    public enum BookingStatus {
        CONFIRMED,   // 예매 확정
        CANCELLED    // 취소 (Soft Delete)
    }
}
