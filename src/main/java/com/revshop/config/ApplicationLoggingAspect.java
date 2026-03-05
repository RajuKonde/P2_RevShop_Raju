package com.revshop.config;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Log4j2
public class ApplicationLoggingAspect {

    @Around("execution(public * com.revshop..*(..)) && !within(com.revshop.config.ApplicationLoggingAspect)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> declaringType = ((MethodSignature) joinPoint.getSignature()).getDeclaringType();
        Object target = joinPoint.getTarget();
        Class<?> loggerType = target == null ? declaringType : target.getClass();
        Logger logger = LogManager.getLogger(loggerType);

        String methodRef = declaringType.getSimpleName() + "." + joinPoint.getSignature().getName();
        String argumentTypes = formatArgumentTypes(joinPoint.getArgs());

        if (isController(declaringType)) {
            logger.info("Entering {}({})", methodRef, argumentTypes);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Entering {}({})", methodRef, argumentTypes);
        }

        try {
            Object result = joinPoint.proceed();
            if (isController(declaringType)) {
                logger.info("Completed {} -> {}", methodRef, formatResultType(result));
            } else if (logger.isDebugEnabled()) {
                logger.debug("Completed {} -> {}", methodRef, formatResultType(result));
            }
            return result;
        } catch (Throwable ex) {
            logger.error("Failed {} with {}", methodRef, ex.getClass().getSimpleName(), ex);
            throw ex;
        }
    }

    private boolean isController(Class<?> type) {
        String packageName = type.getPackageName();
        return packageName.startsWith("com.revshop.controller");
    }

    private String formatArgumentTypes(Object[] args) {
        if (args == null || args.length == 0) {
            return "no-args";
        }

        return Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
                .collect(Collectors.joining(", "));
    }

    private String formatResultType(Object result) {
        return result == null ? "void" : result.getClass().getSimpleName();
    }
}
