package com.movieapp.backend.movies.dto;

import java.time.LocalDate;

/** Movie card row for {@code GET /api/me/movies}. */
public record MovieLibraryItemDto(
		long movieId,
		long tmdbId,
		String title,
		String posterPath,
		LocalDate releaseDate
) {}
