package org.example.bookingservice.models.services.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.bookingservice.clients.MovieClient;
import org.example.bookingservice.exceptions.MovieNotFoundException;
import org.example.bookingservice.exceptions.MovieServiceException;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieGatewayService {

    private final MovieClient movieClient;

    @CircuitBreaker(name = "movieService", fallbackMethod = "getMovieFallback")
    public MovieResponse getMovieById(Long movieId) {
        try {
            MovieResponse response = movieClient.getMovieById(movieId);
            if (response == null || response.id() == null || response.ticketPrice() == null) {
                throw new MovieNotFoundException(movieId);
            }
            return response;
        } catch (FeignException.NotFound e) {
            throw new MovieNotFoundException(movieId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new MovieNotFoundException(movieId);
            }
            throw new MovieServiceException("Movie service is unavailable", e);
        }
    }

    public MovieResponse getMovieFallback(Long movieId, Throwable throwable) {
        if (throwable instanceof MovieNotFoundException movieNotFoundException) {
            throw movieNotFoundException;
        }
        if (throwable instanceof FeignException feignException && feignException.status() == 404) {
            throw new MovieNotFoundException(movieId);
        }
        if (throwable instanceof MovieServiceException movieServiceException) {
            throw movieServiceException;
        }
        throw new MovieServiceException("Movie service is currently unavailable", throwable);
    }

}
