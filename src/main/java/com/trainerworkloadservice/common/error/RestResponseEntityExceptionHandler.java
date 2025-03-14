package com.trainerworkloadservice.common.error;

import com.trainerworkloadservice.common.exception.*;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

	@ExceptionHandler(NotFoundException.class)
	protected ResponseEntity<ErrorsDetails> handleNotFoundException(Throwable ex, WebRequest request) {
		ErrorsDetails errorDetails = new ErrorsDetails(ex.getMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(UnauthorizedException.class)
	protected ResponseEntity<ErrorsDetails> handleUnauthorizedException(Throwable ex, WebRequest request) {
		ErrorsDetails errorDetails = new ErrorsDetails(ex.getMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
	protected ResponseEntity<ErrorsDetails> handleForbiddenException(Throwable ex, WebRequest request) {
		ErrorsDetails errorDetails = new ErrorsDetails(ex.getMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(BadRequestException.class)
	protected ResponseEntity<ErrorsDetails> handleBadRequestException(Throwable ex, WebRequest request) {
		ErrorsDetails errorDetails = new ErrorsDetails(ex.getMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConflictException.class)
	protected ResponseEntity<ErrorsDetails> handleConflictException(Throwable ex, WebRequest request) {
		ErrorsDetails errorDetails = new ErrorsDetails(ex.getMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
	        @Nullable HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {

		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
		        .map(error -> error.getField() + " " + error.getDefaultMessage()).collect(Collectors.toList());

		errors.addAll(ex.getBindingResult().getGlobalErrors().stream()
		        .map(error -> error.getObjectName() + " " + error.getDefaultMessage()).toList());

		ErrorsDetails errorDetails = new ErrorsDetails(String.join("|", errors));
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
	        @Nullable HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {

		ErrorsDetails errorDetails = new ErrorsDetails(ex.getLocalizedMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorsDetails> handleGenericException(Throwable ex, WebRequest request) {
		logger.error("An error occurred: {}", ex.getMessage(), ex);
		ErrorsDetails errorDetails = new ErrorsDetails(ex.getMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
