package com.trainerworkloadservice.configuration.security;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	private final JwtTokenUtil jwtTokenUtil;

	public JwtRequestFilter(JwtTokenUtil jwtTokenUtil) {
		this.jwtTokenUtil = jwtTokenUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	        throws ServletException, IOException {

		final String requestTokenHeader = request.getHeader("Authorization");
		String transactionId = request.getHeader("X-Transaction-ID");
		if (transactionId == null) {
			transactionId = "unknown";
		}

		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			String jwtToken = requestTokenHeader.substring(7);

			try {
				if (jwtTokenUtil.validateToken(jwtToken)) {
					String serviceName = jwtTokenUtil.getServiceNameFromToken(jwtToken);

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					        serviceName, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE")));

					SecurityContextHolder.getContext().setAuthentication(authentication);
					log.info("Transaction [{}]: Valid JWT token for service: {}", transactionId, serviceName);
				} else {
					log.warn("Transaction [{}]: Invalid JWT token", transactionId);
				}
			} catch (Exception e) {
				log.error("Transaction [{}]: Error validating JWT token: {}", transactionId, e.getMessage());
			}
		} else {
			log.debug("Transaction [{}]: No JWT token found in request headers or token doesn't start with Bearer",
			        transactionId);
		}

		chain.doFilter(request, response);
	}
}
