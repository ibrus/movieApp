package com.movieapp.backend.movies;

import com.movieapp.backend.movies.dto.MovieLibraryItemDto;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeMoviesController {

	private final MovieStateService movieStateService;

	public MeMoviesController(MovieStateService movieStateService) {
		this.movieStateService = movieStateService;
	}

	@GetMapping("/movies")
	public List<MovieLibraryItemDto> listMovies(@RequestParam String list, Principal principal) {
		return movieStateService.listMovies(principal.getName(), list);
	}
}
