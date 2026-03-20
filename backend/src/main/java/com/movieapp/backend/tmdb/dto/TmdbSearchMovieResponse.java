package com.movieapp.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for TMDB "search/movie" response.
 * Docs: https://developer.themoviedb.org/reference/search-movie
 */
public record TmdbSearchMovieResponse(
		int page,
		@JsonProperty("total_results") int totalResults,
		List<TmdbSearchMovieResult> results
) {}

