package com.movieapp.backend.user;

import com.movieapp.backend.security.JwtService;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public UserService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
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

		String token = jwtService.generateToken(
				Map.of("userId", user.getId()),
				user.getEmail()
		);

		return new LoginResponse(token, toDto(user));
	}

	public UserDto getByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return toDto(user);
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

	public record LoginResponse(String token, UserDto user) {}
}

