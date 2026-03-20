package com.movieapp.backend.movies;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
public class MovieSearchController {

	private final MovieSearchService movieSearchService;

	public MovieSearchController(MovieSearchService movieSearchService) {
		this.movieSearchService = movieSearchService;
	}

	@GetMapping("/search")
	public MovieSearchService.MovieSearchResponse search(
			@RequestParam("query") String query,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "language", required = false) String language
	) {
		return movieSearchService.search(query, page, language);
	}
}

