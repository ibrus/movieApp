package com.movieapp.backend.movies;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

	Optional<Movie> findByTmdbId(Long tmdbId);
}
