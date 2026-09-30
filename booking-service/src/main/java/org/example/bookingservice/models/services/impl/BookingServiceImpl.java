package org.example.bookingservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bookingservice.exceptions.MovieNotFoundException;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final MovieGatewayService movieGatewayService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String BOOKING_CREATED_TOPIC = "booking-created";

    private record ItemMovieData(CreateBookingDetailRequest item, MovieResponse movie, double lineTotal) {}

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        List<ItemMovieData> itemMovieDataList = new ArrayList<>();
        double totalAmount = 0.0;

        for (CreateBookingDetailRequest item : request.items()) {
            MovieResponse movie = movieGatewayService.getMovieById(item.movieId());
            if (movie == null || movie.ticketPrice() == null) {
                throw new MovieNotFoundException(item.movieId());
            }
            double lineTotal = movie.ticketPrice() * item.quantity();
            totalAmount += lineTotal;
            itemMovieDataList.add(new ItemMovieData(item, movie, lineTotal));
        }

        Booking booking = Booking.builder()
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .total(totalAmount)
                .status(BookingStatus.PENDING)
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        List<BookingDetailResponse> detailResponses = new ArrayList<>();
        for (ItemMovieData data : itemMovieDataList) {
            BookingDetail detail = BookingDetail.builder()
                    .booking(savedBooking)
                    .movieId(data.item().movieId())
                    .quantity(data.item().quantity())
                    .unitPrice(data.movie().ticketPrice())
                    .build();
            BookingDetail savedDetail = bookingDetailRepository.save(detail);

            detailResponses.add(new BookingDetailResponse(
                    savedDetail.getId(),
                    data.movie().id(),
                    data.movie().title(),
                    data.item().quantity(),
                    data.movie().ticketPrice(),
                    data.lineTotal()
            ));
        }

        kafkaTemplate.send(BOOKING_CREATED_TOPIC, request.customerEmail());

        return new BookingResponse(
                savedBooking.getId(),
                savedBooking.getCustomerName(),
                savedBooking.getCustomerEmail(),
                savedBooking.getTotal(),
                savedBooking.getStatus(),
                detailResponses
        );
    }
}
