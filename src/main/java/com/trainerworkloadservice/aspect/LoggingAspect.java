package com.trainerworkloadservice.aspect;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
	@Around("execution(* com.trainerworkloadservice.TrainerWorkload.adapter.input.web.controller.*.*(..))")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		String transactionId = getTransactionId();

		log.info("Transaction [{}]: Started execution of {}.{}() with arguments: {}", transactionId,
		        joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
		        Arrays.toString(joinPoint.getArgs()));

		Object result;
		try {
			result = joinPoint.proceed();
			long executionTime = System.currentTimeMillis() - start;

			log.info("Transaction [{}]: Completed execution of {}.{}() in {} ms with result: {}", transactionId,
			        joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), executionTime,
			        result);

			return result;
		} catch (Exception e) {
			log.error("Transaction [{}]: Exception in {}.{}() with cause = {}", transactionId,
			        joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
			        e.getMessage() != null ? e.getMessage() : "NULL", e);
			throw e;
		}
	}

	private String getTransactionId() {
		try {
			HttpServletRequest request = ((ServletRequestAttributes) Objects
			        .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
			String transactionId = request.getHeader("X-Transaction-ID");
			if (transactionId == null || transactionId.isEmpty()) {
				transactionId = UUID.randomUUID().toString();
			}
			return transactionId;
		} catch (Exception e) {
			return UUID.randomUUID().toString();
		}
	}
}
