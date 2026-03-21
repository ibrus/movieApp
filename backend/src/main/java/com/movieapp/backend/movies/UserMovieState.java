package com.movieapp.backend.movies;

import com.movieapp.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "user_movie_states",
		uniqueConstraints =
				@UniqueConstraint(
						name = "uk_user_movie_state_user_movie",
						columnNames = {"user_id", "movie_id"}))
@Getter
@Setter
@NoArgsConstructor
public class UserMovieState {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "movie_id", nullable = false)
	private Movie movie;

	@Column(nullable = false)
	private boolean watchlist = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Sentiment sentiment = Sentiment.NONE;
}
