package com.movieapp.backend.auth;

import com.movieapp.backend.user.UserService;
import com.movieapp.backend.user.UserService.LoginRequest;
import com.movieapp.backend.user.UserService.LoginResponse;
import com.movieapp.backend.user.UserService.RegisterRequest;
import com.movieapp.backend.user.UserService.UserDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public UserDto register(@RequestBody RegisterRequest request) {
		return userService.register(request);
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request) {
		return userService.login(request);
	}
}

