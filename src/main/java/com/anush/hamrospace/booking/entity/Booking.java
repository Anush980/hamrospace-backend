package com.anush.hamrospace.booking.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.anush.hamrospace.booking.enums.BookingFeeStatus;
import com.anush.hamrospace.booking.enums.BookingStatus;
import com.anush.hamrospace.booking.enums.PaymentMethod;
import com.anush.hamrospace.common.entity.BaseEntity;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "bookings")
public class Booking extends BaseEntity {
    
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @Column(name = "move_in_date", nullable = false)
    private Instant moveInDate;

    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent; // snapshot of rent at booking time

    @Column(name = "booking_fee_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal bookingFeeAmount; 

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_fee_status", nullable = false)
    private BookingFeeStatus bookingFeeStatus = BookingFeeStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod= PaymentMethod.OTHER;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String cancellationReason;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
