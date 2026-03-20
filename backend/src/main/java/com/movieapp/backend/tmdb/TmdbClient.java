package com.movieapp.backend.tmdb;

import com.movieapp.backend.tmdb.dto.TmdbSearchMovieResponse;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TmdbClient {

	private static final Logger log = LoggerFactory.getLogger(TmdbClient.class);

	private final TmdbProperties properties;
	private final RestClient restClient;

	public TmdbClient(TmdbProperties properties) {
		this.properties = Objects.requireNonNull(properties, "tmdb properties must not be null");

		this.restClient = RestClient.builder()
				.baseUrl(properties.getBaseUrl())
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	public TmdbSearchMovieResponse searchMovie(String query, int page, String language) {
		if (properties.getKey() == null || properties.getKey().isBlank()) {
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					"TMDB API key is not configured (tmdb.api.key)"
			);
		}

		String safeLanguage = (language == null || language.isBlank()) ? "en-US" : language;

		try {
			return restClient.get()
					.uri(uriBuilder -> {
						// include_adult is required by TMDB's search/movie endpoint.
						return uriBuilder.path("/3/search/movie")
								.queryParam("query", query)
								.queryParam("page", page)
								.queryParam("language", safeLanguage)
								.queryParam("include_adult", true)
								.queryParam("api_key", properties.getKey())
								.build();
					})
					.retrieve()
					.onStatus(HttpStatusCode::isError, (request, response) -> {
						HttpStatusCode status = response.getStatusCode();
						log.warn("TMDB request failed. status={}", status.value());
						throw new ResponseStatusException(
								HttpStatus.BAD_GATEWAY,
								"TMDB request failed with status " + status.value()
						);
					})
					.body(TmdbSearchMovieResponse.class);
		} catch (RestClientException e) {
			log.warn("TMDB request failed due to RestClientException: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "TMDB request failed", e);
		}
	}
}
