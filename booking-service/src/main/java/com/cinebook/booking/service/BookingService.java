package com.cinebook.booking.service;

import com.cinebook.booking.dto.BookingResponse;
import com.cinebook.booking.dto.CreateBookingRequest;

import java.util.List;

public interface BookingService {

    List<BookingResponse> getAllBookings();

    BookingResponse createBooking(CreateBookingRequest request);
}
