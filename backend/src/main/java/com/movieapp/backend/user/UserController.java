package com.movieapp.backend.user;

import com.movieapp.backend.user.UserService.UserDto;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public UserDto me(Principal principal) {
		return userService.getByEmail(principal.getName());
	}
}
