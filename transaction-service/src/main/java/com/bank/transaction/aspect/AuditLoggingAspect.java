package com.bank.transaction.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Enterprise Audit Logging Aspect (AOP)
 * Automatically intercepts and logs execution time and parameters for all Controller and Service methods
 * without polluting the core business logic.
 */
@Aspect
@Component
@Slf4j
public class AuditLoggingAspect {

    @Around("execution(* com.bank.transaction.controller.*.*(..)) || execution(* com.bank.transaction.service.*.*(..))")
    public Object logAuditTrail(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.info("[AUDIT-START] Executing {}.{} with arguments: {}", className, methodName, Arrays.toString(joinPoint.getArgs()));

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("[AUDIT-FAILURE] Exception in {}.{}: {}", className, methodName, ex.getMessage());
            throw ex;
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("[AUDIT-SUCCESS] Completed {}.{} in {} ms", className, methodName, executionTime);
        
        return result;
    }
}
