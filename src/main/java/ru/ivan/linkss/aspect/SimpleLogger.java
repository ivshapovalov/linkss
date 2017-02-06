package ru.ivan.linkss.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

public class SimpleLogger {

    public Object log (ProceedingJoinPoint call) throws Throwable {
        try {
            return call.proceed();
        } finally {
            System.out.println("ASPECT.LOGGER: " + call.toShortString()+" called!");

        }
    }
}
