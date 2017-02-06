package ru.ivan.linkss.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Aspect
public class SimpleLogger {

    @Pointcut("execution(* ru.ivan.linkss.repository.*(..))")
    public Object log (ProceedingJoinPoint call) throws Throwable {
        try {
            return call.proceed();
        } finally {
            System.out.println("ASPECT.LOGGER: " + call.toShortString()+" called!");

        }
    }
}
