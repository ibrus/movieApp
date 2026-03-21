package com.movieapp.backend.movies;

import com.movieapp.backend.movies.dto.MovieLibraryItemDto;
import com.movieapp.backend.movies.dto.UpdateMovieStateRequest;
import com.movieapp.backend.user.User;
import com.movieapp.backend.user.UserRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MovieStateService {

	private final UserRepository userRepository;
	private final MovieRepository movieRepository;
	private final UserMovieStateRepository userMovieStateRepository;

	public MovieStateService(
			UserRepository userRepository,
			MovieRepository movieRepository,
			UserMovieStateRepository userMovieStateRepository
	) {
		this.userRepository = Objects.requireNonNull(userRepository);
		this.movieRepository = Objects.requireNonNull(movieRepository);
		this.userMovieStateRepository = Objects.requireNonNull(userMovieStateRepository);
	}

	/** Upserts TMDB snapshot and per-user watchlist + sentiment (LIKE/DISLIKE exclusive; NONE clears). */
	@Transactional
	public MovieStateResult updateState(String userEmail, long tmdbId, UpdateMovieStateRequest request) {
		User user =
				userRepository
						.findByEmail(userEmail)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		Movie movie = upsertMovieSnapshot(tmdbId, request);

		UserMovieState state =
				userMovieStateRepository
						.findByUser_IdAndMovie_Id(user.getId(), movie.getId())
						.orElseGet(
								() -> {
									UserMovieState s = new UserMovieState();
									s.setUser(user);
									s.setMovie(movie);
									return s;
								});

		state.setWatchlist(request.watchlist());
		state.setSentiment(request.sentiment());

		userMovieStateRepository.save(state);

		return new MovieStateResult(
				movie.getTmdbId(),
				movie.getTitle(),
				movie.getPosterPath(),
				movie.getReleaseDate(),
				state.isWatchlist(),
				state.getSentiment());
	}

	/** Lists saved movies for the current user; {@code list} must be {@code watchlist}, {@code liked}, or {@code disliked}. */
	@Transactional(readOnly = true)
	public List<MovieLibraryItemDto> listMovies(String userEmail, String list) {
		User user =
				userRepository
						.findByEmail(userEmail)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		String key = list == null ? "" : list.trim().toLowerCase();
		List<UserMovieState> rows =
				switch (key) {
					case "watchlist" -> userMovieStateRepository.findWatchlistForUser(user.getId());
					case "liked" -> userMovieStateRepository.findLikedForUser(user.getId());
					case "disliked" -> userMovieStateRepository.findDislikedForUser(user.getId());
					default ->
							throw new ResponseStatusException(
									HttpStatus.BAD_REQUEST,
									"Invalid list parameter; expected watchlist, liked, or disliked");
				};

		return rows.stream().map(MovieStateService::toLibraryItem).toList();
	}

	private static MovieLibraryItemDto toLibraryItem(UserMovieState state) {
		Movie m = state.getMovie();
		return new MovieLibraryItemDto(m.getId(), m.getTmdbId(), m.getTitle(), m.getPosterPath(), m.getReleaseDate());
	}

	private Movie upsertMovieSnapshot(long tmdbId, UpdateMovieStateRequest request) {
		Movie movie =
				movieRepository
						.findByTmdbId(tmdbId)
						.orElseGet(
								() -> {
									Movie m = new Movie();
									m.setTmdbId(tmdbId);
									return m;
								});

		movie.setTitle(request.title());
		movie.setPosterPath(blankToNull(request.posterPath()));
		movie.setReleaseDate(parseReleaseDate(request.releaseDate()));

		return movieRepository.save(movie);
	}

	private static String blankToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	private static LocalDate parseReleaseDate(String releaseDate) {
		if (releaseDate == null) {
			return null;
		}
		String t = releaseDate.trim();
		if (t.isEmpty()) {
			return null;
		}
		try {
			return LocalDate.parse(t);
		} catch (DateTimeParseException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "releaseDate must be ISO-8601 (yyyy-MM-dd)");
		}
	}

	public record MovieStateResult(
			long tmdbId,
			String title,
			String posterPath,
			LocalDate releaseDate,
			boolean watchlist,
			Sentiment sentiment
	) {}
}
