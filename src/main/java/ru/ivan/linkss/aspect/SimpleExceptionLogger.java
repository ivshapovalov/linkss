package ru.ivan.linkss.aspect;

import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


public class SimpleExceptionLogger {
    public void logException(JoinPoint call, Throwable t) {

        System.out.println("ASPECT.EXCEPTION-LOGGER: " + t.getMessage());

    }
}
