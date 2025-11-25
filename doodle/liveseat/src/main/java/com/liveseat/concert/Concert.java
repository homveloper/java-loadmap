package com.liveseat.concert;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 공연 Entity
 * Optimistic Locking을 위한 @Version 포함
 */
@Entity
@Table(name = "concerts")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, name = "price_vip")
    private Integer priceVip;

    @Column(nullable = false, name = "price_r")
    private Integer priceR;

    @Column(nullable = false, name = "price_s")
    private Integer priceS;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    // Optimistic Locking for concurrency control
    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // JPA requires default constructor
    protected Concert() {}

    public Concert(String title, String description, LocalDate startDate, LocalDate endDate,
                   Integer priceVip, Integer priceR, Integer priceS, Integer totalSeats) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priceVip = priceVip;
        this.priceR = priceR;
        this.priceS = priceS;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats; // 처음엔 모든 좌석이 available
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

    // Business logic: 좌석 예매
    public boolean reserveSeats(int count) {
        if (availableSeats >= count) {
            availableSeats -= count;
            return true;
        }
        return false;
    }

    // Business logic: 예매 취소
    public void cancelSeats(int count) {
        availableSeats += count;
        if (availableSeats > totalSeats) {
            availableSeats = totalSeats;
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getPriceVip() {
        return priceVip;
    }

    public Integer getPriceR() {
        return priceR;
    }

    public Integer getPriceS() {
        return priceS;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters (for updates)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setPriceVip(Integer priceVip) {
        this.priceVip = priceVip;
    }

    public void setPriceR(Integer priceR) {
        this.priceR = priceR;
    }

    public void setPriceS(Integer priceS) {
        this.priceS = priceS;
    }
}
