package com.trainerworkloadservice.configuration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenUtil {
	@Value("${jwt.secret}")
	private String secret;

	public boolean validateToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

			boolean isValid = !claims.getExpiration().before(new Date());
			if (!isValid) {
				log.warn("JWT token is expired");
			}
			return isValid;
		} catch (Exception e) {
			log.error("Invalid JWT token: {}", e.getMessage());
			return false;
		}
	}

	public String getServiceNameFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

			return claims.getSubject();
		} catch (Exception e) {
			log.error("Error extracting subject from JWT token: {}", e.getMessage());
			return null;
		}
	}
}
