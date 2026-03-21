package com.movieapp.backend.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		return ResponseEntity.badRequest().body(detail);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		String message =
				ex.getBindingResult().getFieldErrors().stream()
						.map(err -> err.getField() + ": " + err.getDefaultMessage())
						.findFirst()
						.orElse("Validation failed");
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
		return ResponseEntity.badRequest().body(detail);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
		ProblemDetail detail =
				ProblemDetail.forStatusAndDetail(
						HttpStatus.CONFLICT, "Request conflicts with existing data");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(detail);
	}
}
