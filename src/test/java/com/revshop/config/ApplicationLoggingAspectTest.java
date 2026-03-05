package com.revshop.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationLoggingAspectTest {

    @Test
    public void logMethod_proceedsAndReturnsOriginalResult() throws Throwable {
        ApplicationLoggingAspect aspect = new ApplicationLoggingAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(SampleTarget.class);
        when(signature.getName()).thenReturn("sampleMethod");
        when(joinPoint.getTarget()).thenReturn(new SampleTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"value", 10});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.logMethod(joinPoint);

        assertEquals("ok", result);
        verify(joinPoint).proceed();
    }

    private static class SampleTarget {
    }
}
