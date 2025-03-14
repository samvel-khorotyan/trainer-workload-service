package com.trainerworkloadservice.unit.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.trainerworkloadservice.aspect.LoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoggingAspectTest {
	@Mock
	private ProceedingJoinPoint joinPoint;

	@Mock
	private Signature signature;

	@InjectMocks
	private LoggingAspect loggingAspect;

	@BeforeEach
  public void setUp() {
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("TestController");
    when(signature.getName()).thenReturn("testMethod");
    when(joinPoint.getArgs()).thenReturn(new Object[] {"arg1", "arg2"});
  }

	@Test
	public void testLogAroundSuccess() throws Throwable {
		String expectedResult = "Success";
		when(joinPoint.proceed()).thenReturn(expectedResult);

		Object result = loggingAspect.logAround(joinPoint);

		assertEquals(expectedResult, result);
		verify(joinPoint).proceed();
		verify(signature, atLeastOnce()).getDeclaringTypeName();
		verify(signature, atLeastOnce()).getName();
	}

	@Test
	public void testLogAroundException() throws Throwable {
		RuntimeException expectedException = new RuntimeException("Test exception");
		when(joinPoint.proceed()).thenThrow(expectedException);

		Exception exception = assertThrows(RuntimeException.class, () -> loggingAspect.logAround(joinPoint));

		assertEquals("Test exception", exception.getMessage());
		verify(joinPoint).proceed();
	}

	@Test
  public void testLogAroundCompletesFullExecution() throws Throwable {
    when(joinPoint.proceed()).thenReturn("result");

    Object result = loggingAspect.logAround(joinPoint);

    assertEquals("result", result);
    verify(joinPoint).proceed();
  }
}
