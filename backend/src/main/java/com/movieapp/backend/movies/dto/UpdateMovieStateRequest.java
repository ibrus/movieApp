package com.movieapp.backend.movies.dto;

import com.movieapp.backend.movies.Sentiment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code PUT /api/movies/{tmdbId}/state}.
 * <p>
 * {@code releaseDate} is optional; when present use ISO-8601 date format {@code yyyy-MM-dd}
 * (same as TMDB's {@code release_date}).
 */
public record UpdateMovieStateRequest(
		@NotBlank String title,
		String posterPath,
		String releaseDate,
		@NotNull Boolean watchlist,
		@NotNull Sentiment sentiment
) {}
