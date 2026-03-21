package com.movieapp.backend.movies;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMovieStateRepository extends JpaRepository<UserMovieState, Long> {

	Optional<UserMovieState> findByUser_IdAndMovie_Id(Long userId, Long movieId);

	@Query(
			"SELECT u FROM UserMovieState u JOIN FETCH u.movie WHERE u.user.id = :userId AND u.watchlist = true")
	List<UserMovieState> findWatchlistForUser(@Param("userId") Long userId);

	@Query(
			"SELECT u FROM UserMovieState u JOIN FETCH u.movie WHERE u.user.id = :userId AND u.sentiment = com.movieapp.backend.movies.Sentiment.LIKE")
	List<UserMovieState> findLikedForUser(@Param("userId") Long userId);

	@Query(
			"SELECT u FROM UserMovieState u JOIN FETCH u.movie WHERE u.user.id = :userId AND u.sentiment = com.movieapp.backend.movies.Sentiment.DISLIKE")
	List<UserMovieState> findDislikedForUser(@Param("userId") Long userId);
}
