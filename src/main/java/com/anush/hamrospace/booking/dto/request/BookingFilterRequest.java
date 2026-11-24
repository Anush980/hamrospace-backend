package com.anush.hamrospace.booking.dto.request;

import com.anush.hamrospace.booking.enums.BookingFeeStatus;
import com.anush.hamrospace.booking.enums.BookingStatus;
import com.anush.hamrospace.booking.enums.PaymentMethod;

public record BookingFilterRequest(
    String keyword,
    BookingFeeStatus bookingFeeStatus,
    BookingStatus bookingStatus,
    PaymentMethod paymentMethod
) {
    
}
