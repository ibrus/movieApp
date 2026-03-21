package com.movieapp.backend.movies;

import com.movieapp.backend.movies.dto.UpdateMovieStateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@Validated
public class MovieStateController {

	private final MovieStateService movieStateService;

	public MovieStateController(MovieStateService movieStateService) {
		this.movieStateService = movieStateService;
	}

	@PutMapping("/{tmdbId}/state")
	public MovieStateService.MovieStateResult updateState(
			@PathVariable @Positive long tmdbId,
			@Valid @RequestBody UpdateMovieStateRequest body,
			Principal principal) {
		return movieStateService.updateState(principal.getName(), tmdbId, body);
	}
}
