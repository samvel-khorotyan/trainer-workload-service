package com.trainerworkloadservice.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trainerworkloadservice.common.error.ErrorsDetails;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {
	@Serial
	private static final long serialVersionUID = -7858869558953243875L;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
	        AuthenticationException authException) throws IOException {
		log.error("Unauthorized error: {}", authException.getMessage());

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		ErrorsDetails errorDetails = new ErrorsDetails("Unauthorized: " + authException.getMessage());
		response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
	}
}
