package com.trainerworkloadservice.unit.common.error;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.trainerworkloadservice.common.error.ErrorsDetails;
import com.trainerworkloadservice.common.error.RestResponseEntityExceptionHandler;
import com.trainerworkloadservice.common.exception.*;
import java.lang.reflect.Method;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.WebRequest;

class RestResponseEntityExceptionHandlerTest {
	private final RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();

	@Test
	void handleNotFoundException_ShouldReturnNotFoundResponse() throws Exception {
		String errorMessage = "Resource not found";
		NotFoundException exception = new NotFoundException(errorMessage);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleNotFoundException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status code should be 404 NOT_FOUND");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match the exception message");
	}

	@Test
	void handleNotFoundException_ShouldLogError() throws Exception {
		String errorMessage = "Resource not found with id: 123";
		NotFoundException exception = new NotFoundException(errorMessage);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleNotFoundException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status code should be 404 NOT_FOUND");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertTrue(response.getBody().getMessage().contains("123"),
		        "Error message should contain the provided resource ID");
	}

	@Test
	void handleNotFoundException_ShouldHandleEmptyMessage() throws Exception {
		NotFoundException exception = new NotFoundException("");
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleNotFoundException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status code should be 404 NOT_FOUND");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals("", response.getBody().getMessage(), "Error message should be empty");
	}

	@Test
	void handleUnauthorizedException_ShouldReturnUnauthorizedResponse() throws Exception {
		String errorMessage = "Unauthorized access";
		UnauthorizedException exception = new UnauthorizedException(errorMessage);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleUnauthorizedException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Status code should be 401 UNAUTHORIZED");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match the exception message");
	}

	@Test
	void handleUnauthorizedException_ShouldHandleEmptyMessage() throws Exception {
		UnauthorizedException exception = new UnauthorizedException("");
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleUnauthorizedException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Status code should be 401 UNAUTHORIZED");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals("", response.getBody().getMessage(), "Error message should be empty");
	}

	@Test
	void handleForbiddenException_ShouldReturnForbiddenResponse() throws Exception {
		String errorMessage = "Access is forbidden";
		ForbiddenException exception = new ForbiddenException(errorMessage);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleForbiddenException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(403, response.getStatusCodeValue(), "Status code should be 403 FORBIDDEN");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match the exception message");
	}

	@Test
	void handleForbiddenException_ShouldHandleEmptyMessage() throws Exception {
		ForbiddenException exception = new ForbiddenException("");
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleForbiddenException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(403, response.getStatusCodeValue(), "Status code should be 403 FORBIDDEN");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals("", response.getBody().getMessage(), "Error message should be empty");
	}

	@Test
	void handleForbiddenException_ShouldHandleNullMessage() throws Exception {
		ForbiddenException exception = new ForbiddenException(null);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleForbiddenException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(403, response.getStatusCodeValue(), "Status code should be 403 FORBIDDEN");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertNull(response.getBody().getMessage(), "Error message should be null");
	}

	@Test
	void handleBadRequestException_ShouldReturnBadRequestResponse() throws Exception {
		String errorMessage = "Invalid request data";
		BadRequestException exception = new BadRequestException(errorMessage);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleBadRequestException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(400, response.getStatusCodeValue(), "Status code should be 400 BAD_REQUEST");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match the exception message");
	}

	@Test
	void handleBadRequestException_ShouldHandleEmptyMessage() throws Exception {
		BadRequestException exception = new BadRequestException("");
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleBadRequestException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(400, response.getStatusCodeValue(), "Status code should be 400 BAD_REQUEST");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals("", response.getBody().getMessage(), "Error message should be empty");
	}

	@Test
	void handleBadRequestException_ShouldHandleNullMessage() throws Exception {
		BadRequestException exception = new BadRequestException(null);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleBadRequestException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(400, response.getStatusCodeValue(), "Status code should be 400 BAD_REQUEST");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertNull(response.getBody().getMessage(), "Error message should be null");
	}

	@Test
	void handleConflictException_ShouldReturnConflictResponse() throws Exception {
		String errorMessage = "Conflict occurred while processing the request";
		ConflictException exception = new ConflictException(errorMessage);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleConflictException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(409, response.getStatusCodeValue(), "Status code should be 409 CONFLICT");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match the exception message");
	}

	@Test
	void handleConflictException_ShouldHandleEmptyMessage() throws Exception {
		ConflictException exception = new ConflictException("");
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleConflictException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(409, response.getStatusCodeValue(), "Status code should be 409 CONFLICT");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertEquals("", response.getBody().getMessage(), "Error message should be empty");
	}

	@Test
	void handleConflictException_ShouldHandleNullMessage() throws Exception {
		ConflictException exception = new ConflictException(null);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleConflictException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(409, response.getStatusCodeValue(), "Status code should be 409 CONFLICT");
		assertNotNull(response.getBody(), "Response body should not be null");
		assertNull(response.getBody().getMessage(), "Error message should be null");
	}

	@Test
	void handleHttpMessageNotReadable_ShouldReturnBadRequestResponse() throws Exception {
		HttpInputMessage mockInputMessage = mock(HttpInputMessage.class);
		HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid JSON format",
		        mockInputMessage);
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<Object> response = invokeHandleHttpMessageNotReadable(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code should be 400 BAD_REQUEST");
		assertNotNull(response.getBody(), "Response body should not be null");

		ErrorsDetails errorDetails = (ErrorsDetails) response.getBody();
		assertNotNull(errorDetails, "Error details should not be null");
		assertEquals("Invalid JSON format", errorDetails.getMessage(), "Error message should match exception message");
	}

	@Test
	void handleGenericException_NullPointerException() throws Exception {
		Throwable exception = new NullPointerException("Null pointer exception occurred");
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleGenericException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(500, response.getStatusCodeValue(), "Status should be 500 INTERNAL_SERVER_ERROR");
		assertEquals("Null pointer exception occurred", Objects.requireNonNull(response.getBody()).getMessage(),
		        "Error message should match");
	}

	@Test
	void handleGenericException_CustomException() throws Exception {
		Throwable exception = new RuntimeException("Custom runtime exception");
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleGenericException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(500, response.getStatusCodeValue(), "Status should be 500 INTERNAL_SERVER_ERROR");
		assertEquals("Custom runtime exception", Objects.requireNonNull(response.getBody()).getMessage(),
		        "Error message should match");
	}

	@Test
	void handleGenericException_UnexpectedException() throws Exception {
		Throwable exception = new IllegalArgumentException("Illegal argument exception");
		WebRequest mockRequest = mock(WebRequest.class);

		ResponseEntity<ErrorsDetails> response = invokeHandleGenericException(exception, mockRequest);

		assertNotNull(response, "Response should not be null");
		assertEquals(500, response.getStatusCodeValue(), "Status should be 500 INTERNAL_SERVER_ERROR");
		assertEquals("Illegal argument exception", Objects.requireNonNull(response.getBody()).getMessage(),
		        "Error message should match");
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<ErrorsDetails> invokeHandleNotFoundException(NotFoundException exception, WebRequest request)
	        throws Exception {
		Method method = RestResponseEntityExceptionHandler.class.getDeclaredMethod("handleNotFoundException",
		        Throwable.class, WebRequest.class);
		method.setAccessible(true);
		return (ResponseEntity<ErrorsDetails>) method.invoke(exceptionHandler, exception, request);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<ErrorsDetails> invokeHandleUnauthorizedException(UnauthorizedException exception,
	        WebRequest request) throws Exception {
		Method method = RestResponseEntityExceptionHandler.class.getDeclaredMethod("handleUnauthorizedException",
		        Throwable.class, WebRequest.class);
		method.setAccessible(true);
		return (ResponseEntity<ErrorsDetails>) method.invoke(exceptionHandler, exception, request);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<ErrorsDetails> invokeHandleForbiddenException(ForbiddenException exception,
	        WebRequest request) throws Exception {
		Method method = RestResponseEntityExceptionHandler.class.getDeclaredMethod("handleForbiddenException",
		        Throwable.class, WebRequest.class);
		method.setAccessible(true);
		return (ResponseEntity<ErrorsDetails>) method.invoke(exceptionHandler, exception, request);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<ErrorsDetails> invokeHandleBadRequestException(BadRequestException exception,
	        WebRequest request) throws Exception {
		Method method = RestResponseEntityExceptionHandler.class.getDeclaredMethod("handleBadRequestException",
		        Throwable.class, WebRequest.class);
		method.setAccessible(true);
		return (ResponseEntity<ErrorsDetails>) method.invoke(exceptionHandler, exception, request);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<ErrorsDetails> invokeHandleConflictException(ConflictException exception, WebRequest request)
	        throws Exception {
		Method method = RestResponseEntityExceptionHandler.class.getDeclaredMethod("handleConflictException",
		        Throwable.class, WebRequest.class);
		method.setAccessible(true);
		return (ResponseEntity<ErrorsDetails>) method.invoke(exceptionHandler, exception, request);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<Object> invokeHandleHttpMessageNotReadable(HttpMessageNotReadableException exception,
	        WebRequest request) throws Exception {
		Method method = RestResponseEntityExceptionHandler.class.getDeclaredMethod("handleHttpMessageNotReadable",
		        HttpMessageNotReadableException.class, HttpHeaders.class, HttpStatus.class, WebRequest.class);
		method.setAccessible(true);
		return (ResponseEntity<Object>) method.invoke(exceptionHandler, exception, null, HttpStatus.BAD_REQUEST,
		        request);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<ErrorsDetails> invokeHandleGenericException(Throwable exception, WebRequest request)
	        throws Exception {
		Method method = RestResponseEntityExceptionHandler.class.getDeclaredMethod("handleGenericException",
		        Throwable.class, WebRequest.class);
		method.setAccessible(true);
		return (ResponseEntity<ErrorsDetails>) method.invoke(exceptionHandler, exception, request);
	}
}
