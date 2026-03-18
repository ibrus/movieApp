package com.movieapp.backend.user;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	public UserDto register(RegisterRequest request) {
		userRepository.findByEmail(request.email())
				.ifPresent(existing -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
				});

		User user = new User();
		user.setEmail(request.email());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setDisplayName(request.displayName());
		if (user.getCreatedAt() == null) {
			user.setCreatedAt(Instant.now());
		}

		User saved = userRepository.save(user);
		return toDto(saved);
	}

	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}

		return new LoginResponse("login ok", toDto(user));
	}

	private UserDto toDto(User user) {
		return new UserDto(
				user.getId(),
				user.getEmail(),
				user.getDisplayName(),
				user.getCreatedAt()
		);
	}

	public record RegisterRequest(String email, String password, String displayName) {}

	public record LoginRequest(String email, String password) {}

	public record UserDto(Long id, String email, String displayName, Instant createdAt) {}

	public record LoginResponse(String message, UserDto user) {}
}

