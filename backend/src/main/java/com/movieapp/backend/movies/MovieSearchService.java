package com.movieapp.backend.movies;

import com.movieapp.backend.tmdb.TmdbClient;
import com.movieapp.backend.tmdb.dto.TmdbSearchMovieResult;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MovieSearchService {

	private static final int DEFAULT_OVERVIEW_SNIPPET_LENGTH = 180;
	private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";

	private final TmdbClient tmdbClient;

	public MovieSearchService(TmdbClient tmdbClient) {
		this.tmdbClient = Objects.requireNonNull(tmdbClient, "tmdbClient must not be null");
	}

	public MovieSearchResponse search(String query, int page, String language) {
		if (query == null || query.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must not be blank");
		}
		if (page < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be >= 1");
		}

		var tmdbResponse = tmdbClient.searchMovie(query, page, language);

		List<MovieSearchResult> mappedResults = (tmdbResponse.results() == null ? List.<TmdbSearchMovieResult>of() : tmdbResponse.results())
				.stream()
				.map(this::toResult)
				.toList();

		return new MovieSearchResponse(tmdbResponse.page(), tmdbResponse.totalResults(), mappedResults);
	}

	private MovieSearchResult toResult(TmdbSearchMovieResult result) {
		String releaseYear = extractReleaseYear(result.releaseDate());
		String posterUrl = toPosterUrl(result.posterPath());
		String overviewSnippet = toOverviewSnippet(result.overview());

		return new MovieSearchResult(
				result.id(),
				result.title(),
				releaseYear,
				result.voteAverage(),
				overviewSnippet,
				posterUrl
		);
	}

	private static String extractReleaseYear(String releaseDate) {
		if (releaseDate == null) {
			return null;
		}

		String trimmed = releaseDate.trim();
		if (trimmed.length() < 4) {
			return null;
		}

		return trimmed.substring(0, 4);
	}

	private static String toOverviewSnippet(String overview) {
		if (overview == null) {
			return null;
		}

		String trimmed = overview.trim();
		if (trimmed.isEmpty()) {
			return null;
		}

		if (trimmed.length() <= DEFAULT_OVERVIEW_SNIPPET_LENGTH) {
			return trimmed;
		}

		// Keep it simple: UI can always show the full overview later.
		return trimmed.substring(0, DEFAULT_OVERVIEW_SNIPPET_LENGTH) + "...";
	}

	private static String toPosterUrl(String posterPath) {
		if (posterPath == null) {
			return null;
		}

		String trimmed = posterPath.trim();
		if (trimmed.isEmpty()) {
			return null;
		}

		return POSTER_BASE_URL + trimmed;
	}

	public record MovieSearchResponse(
			int page,
			int totalResults,
			List<MovieSearchResult> results
	) {}

	public record MovieSearchResult(
			long id,
			String title,
			String releaseYear,
			double voteAverage,
			String overview,
			String posterUrl
	) {}
}

