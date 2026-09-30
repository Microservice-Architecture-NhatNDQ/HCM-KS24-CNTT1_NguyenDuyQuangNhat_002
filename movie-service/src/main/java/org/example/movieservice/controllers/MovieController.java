package org.example.movieservice.controllers;

import org.example.movieservice.exceptions.MovieNotFoundException;
import org.example.movieservice.models.entities.Movie;
import org.example.movieservice.models.services.MovieService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        Movie result = movieService.getMovieById(id);
        if (result == null) {
            throw new MovieNotFoundException(id);
        }

        return ResponseEntity.ok(result);
    }
}
