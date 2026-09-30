package org.example.bookingservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.exceptions.MovieServiceException;
import org.example.bookingservice.models.constants.BookingStatus;
import org.example.bookingservice.models.dto.requests.CreateBookingDetailRequest;
import org.example.bookingservice.models.dto.requests.CreateBookingRequest;
import org.example.bookingservice.models.dto.responses.BookingDetailResponse;
import org.example.bookingservice.models.dto.responses.BookingResponse;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.example.bookingservice.models.entities.Booking;
import org.example.bookingservice.models.entities.BookingDetail;
import org.example.bookingservice.models.repositories.BookingDetailRepository;
import org.example.bookingservice.models.repositories.BookingRepository;
import org.example.bookingservice.models.services.BookingService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

        private final BookingRepository bookingRepository;
        private final BookingDetailRepository bookingDetailRepository;
        private final MovieGatewayService movieGatewayService;

        @Override
        @Transactional
        public BookingResponse createBooking(CreateBookingRequest request) {
                List<BookingDetailResponse> detailResponses = new ArrayList<>();

                request.items().forEach(item -> {
                        double lineTotal = 0.0;
                        MovieResponse movie = movieGatewayService.getMovieById(item.movieId());
                        Double price = movie.ticketPrice();
                        BookingDetailResponse response = new BookingDetailResponse(0, item.movieId(), movie.title(), item.quantity(), price * item.quantity(), price * item.quantity());
                        detailResponses.add(response);
                });

                throw new UnsupportedOperationException();
        }
}
