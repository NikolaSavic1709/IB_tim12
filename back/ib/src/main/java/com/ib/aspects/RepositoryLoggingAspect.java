package com.ib.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.ib.utils.LogIdGenerator.setLogId;

@Aspect
@Component
public class RepositoryLoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(RepositoryLoggingAspect.class);
    @Pointcut("execution(* com.ib.repository..*(..))")
    public void repositoryMethodsPointcut() {
    }
    @Before(value = "repositoryMethodsPointcut()")
    public void logRepositoryMethodsBefore(JoinPoint joinPoint) {
        setLogId();
        Object[] args = joinPoint.getArgs();
        StringBuilder log= new StringBuilder("BEFORE, "+joinPoint.getSignature().getDeclaringTypeName()+" method called: " + joinPoint.getSignature().getName() + " args:");
        for(Object arg: args)
        {
            log.append(" ").append(arg.toString());
        }
        logger.info(log.toString());
        MDC.remove("logId");
    }
    @AfterReturning(value = "repositoryMethodsPointcut()", returning = "result")
    public void logRepositoryMethodsAfter(JoinPoint joinPoint, Object result) {
        setLogId();
        if (result instanceof Optional<?> optionalResult) {
            if (optionalResult.isPresent()) {
                Object value = optionalResult.get();
                logger.info("AFTER, {} method called: {}, {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), value);
            } else {
                logger.info("AFTER, {} method called: {}, Result is empty", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            }
        } else {
            logger.info("AFTER, {} method called: {}, {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), result.toString());
        }
        MDC.remove("logId");
    }
    @AfterThrowing(value = "repositoryMethodsPointcut()", throwing = "ex")
    public void handleException(JoinPoint joinPoint, Exception ex) {
        setLogId();
        logger.error("AFTER, Exception  in "+ joinPoint.getSignature().getDeclaringTypeName()+ " method: {}", joinPoint.getSignature().getName());
        MDC.remove("logId");
    }
}

