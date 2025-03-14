package com.trainerworkloadservice.unit.configuration.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.trainerworkloadservice.configuration.security.JwtRequestFilter;
import com.trainerworkloadservice.configuration.security.JwtTokenUtil;
import java.lang.reflect.Method;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {
	@Mock
	private JwtTokenUtil jwtTokenUtil;

	@InjectMocks
	private JwtRequestFilter jwtRequestFilter;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void testDoFilterInternal() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		request.addHeader("Authorization", "Bearer validToken");
		request.addHeader("X-Transaction-ID", "testTransaction");

		when(jwtTokenUtil.validateToken("validToken")).thenReturn(true);
		when(jwtTokenUtil.getServiceNameFromToken("validToken")).thenReturn("testService");

		Method doFilterInternalMethod = JwtRequestFilter.class.getDeclaredMethod("doFilterInternal",
		        HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
		doFilterInternalMethod.setAccessible(true);

		doFilterInternalMethod.invoke(jwtRequestFilter, request, response, filterChain);

		verify(jwtTokenUtil).validateToken("validToken");
		verify(jwtTokenUtil).getServiceNameFromToken("validToken");
		verify(filterChain).doFilter(request, response);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertEquals("testService", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}

	@Test
	void testDoFilterInternalWithInvalidToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		request.addHeader("Authorization", "Bearer invalidToken");
		request.addHeader("X-Transaction-ID", "testTransaction");

		when(jwtTokenUtil.validateToken("invalidToken")).thenReturn(false);

		Method doFilterInternalMethod = JwtRequestFilter.class.getDeclaredMethod("doFilterInternal",
		        HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
		doFilterInternalMethod.setAccessible(true);

		doFilterInternalMethod.invoke(jwtRequestFilter, request, response, filterChain);

		verify(jwtTokenUtil).validateToken("invalidToken");
		verify(jwtTokenUtil, never()).getServiceNameFromToken(anyString());
		verify(filterChain).doFilter(request, response);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void testDoFilterInternalWithNoToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		Method doFilterInternalMethod = JwtRequestFilter.class.getDeclaredMethod("doFilterInternal",
		        HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
		doFilterInternalMethod.setAccessible(true);

		doFilterInternalMethod.invoke(jwtRequestFilter, request, response, filterChain);

		verify(jwtTokenUtil, never()).validateToken(anyString());
		verify(jwtTokenUtil, never()).getServiceNameFromToken(anyString());
		verify(filterChain).doFilter(request, response);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
}
