package com.movieapp.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final javax.crypto.SecretKey signingKey;
	private final long expiresInSeconds;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expires-in-seconds}") long expiresInSeconds
	) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("JWT secret is not configured (app.jwt.secret)");
		}
		byte[] keyBytes = Decoders.BASE64URL.decode(secret);
		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
		this.expiresInSeconds = expiresInSeconds;
	}

	public String generateToken(UserDetails userDetails) {
		return generateToken(Map.of(), userDetails.getUsername());
	}

	public String generateToken(Map<String, Object> extraClaims, String subject) {
		Instant now = Instant.now();
		Instant expiry = now.plusSeconds(expiresInSeconds);

		return Jwts.builder()
				.claims(extraClaims)
				.subject(subject)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.signWith(signingKey)
				.compact();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractSubject(token);
		return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	public String extractSubject(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isTokenExpired(String token) {
		Date expiration = extractClaim(token, Claims::getExpiration);
		return expiration.before(new Date());
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
