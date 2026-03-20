package com.movieapp.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a single TMDB "search/movie" result item.
 * Docs: https://developer.themoviedb.org/reference/search-movie
 */
public record TmdbSearchMovieResult(
		long id,
		String title,
		@JsonProperty("release_date") String releaseDate,
		String overview,
		@JsonProperty("poster_path") String posterPath,
		@JsonProperty("vote_average") double voteAverage
) {}

