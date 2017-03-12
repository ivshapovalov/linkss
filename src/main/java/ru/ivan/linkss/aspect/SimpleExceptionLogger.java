package ru.ivan.linkss.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SimpleExceptionLogger {
    @AfterThrowing(
            pointcut = "execution(* ru.ivan.linkss.repository.RedisOneDBLinkRepositoryImpl.*(..))",
            throwing = "t")
    public void logException(JoinPoint call, Throwable t) {
        System.out.println("ASPECT.EXCEPTION-REPO-LOGGER: " + t.getMessage());
    }
}
