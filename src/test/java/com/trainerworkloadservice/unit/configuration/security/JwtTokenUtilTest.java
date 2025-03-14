package com.trainerworkloadservice.unit.configuration.security;

import static org.junit.jupiter.api.Assertions.*;

import com.trainerworkloadservice.configuration.security.JwtTokenUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {
	@InjectMocks
	private JwtTokenUtil jwtTokenUtil;

	private String serviceName;
	private SecretKey secretKey;

	@BeforeEach
	void setUp() {
		String secret = "thisIsAVeryLongSecretKeyForTestingPurposesOnly12345678901234567890";
		serviceName = "test-service";
		secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

		ReflectionTestUtils.setField(jwtTokenUtil, "secret", secret);
	}

	@Test
	void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
		String token = createValidToken(serviceName, new Date(System.currentTimeMillis() + 60000));

		boolean result = jwtTokenUtil.validateToken(token);

		assertTrue(result);
	}

	@Test
	void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
		String token = createValidToken(serviceName, new Date(System.currentTimeMillis() - 60000));

		boolean result = jwtTokenUtil.validateToken(token);

		assertFalse(result);
	}

	@Test
	void validateToken_ShouldReturnFalse_WhenTokenHasInvalidSignature() {
		String differentSecret = "differentSecretKeyForTestingInvalidSignature12345678901234567890";
		SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));

		String token = Jwts.builder().setSubject(serviceName).setIssuedAt(new Date())
		        .setExpiration(new Date(System.currentTimeMillis() + 60000))
		        .signWith(differentKey, SignatureAlgorithm.HS512).compact();

		boolean result = jwtTokenUtil.validateToken(token);

		assertFalse(result);
	}

	@Test
	void validateToken_ShouldReturnFalse_WhenTokenIsMalformed() {
		String malformedToken = "malformed.token.value";

		boolean result = jwtTokenUtil.validateToken(malformedToken);

		assertFalse(result);
	}

	@Test
	void validateToken_ShouldReturnFalse_WhenTokenIsEmpty() {
		String emptyToken = "";

		boolean result = jwtTokenUtil.validateToken(emptyToken);

		assertFalse(result);
	}

	@Test
	void validateToken_ShouldReturnFalse_WhenTokenIsNull() {
		boolean result = jwtTokenUtil.validateToken(null);

		assertFalse(result);
	}

	@Test
	void getServiceNameFromToken_ShouldReturnServiceName_WhenTokenIsValid() {
		String token = createValidToken(serviceName, new Date(System.currentTimeMillis() + 60000));

		String result = jwtTokenUtil.getServiceNameFromToken(token);

		assertEquals(serviceName, result);
	}

	@Test
	void getServiceNameFromToken_ShouldReturnNull_WhenTokenIsExpired() {
		String token = createValidToken(serviceName, new Date(System.currentTimeMillis() - 60000));

		String result = jwtTokenUtil.getServiceNameFromToken(token);

		assertNull(result);
	}

	@Test
	void getServiceNameFromToken_ShouldReturnNull_WhenTokenHasInvalidSignature() {
		String differentSecret = "differentSecretKeyForTestingInvalidSignature12345678901234567890";
		SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));

		String token = Jwts.builder().setSubject(serviceName).setIssuedAt(new Date())
		        .setExpiration(new Date(System.currentTimeMillis() + 60000))
		        .signWith(differentKey, SignatureAlgorithm.HS512).compact();

		String result = jwtTokenUtil.getServiceNameFromToken(token);

		assertNull(result);
	}

	@Test
	void getServiceNameFromToken_ShouldReturnNull_WhenTokenIsMalformed() {
		String malformedToken = "malformed.token.value";

		String result = jwtTokenUtil.getServiceNameFromToken(malformedToken);

		assertNull(result);
	}

	@Test
	void getServiceNameFromToken_ShouldReturnNull_WhenTokenIsEmpty() {
		String emptyToken = "";

		String result = jwtTokenUtil.getServiceNameFromToken(emptyToken);

		assertNull(result);
	}

	@Test
	void getServiceNameFromToken_ShouldReturnNull_WhenTokenIsNull() {
		String result = jwtTokenUtil.getServiceNameFromToken(null);

		assertNull(result);
	}

	private String createValidToken(String subject, Date expiration) {
		return Jwts.builder().setSubject(subject).setIssuedAt(new Date()).setExpiration(expiration)
		        .signWith(secretKey, SignatureAlgorithm.HS512).compact();
	}
}
