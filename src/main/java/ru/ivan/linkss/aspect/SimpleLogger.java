package ru.ivan.linkss.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class SimpleLogger {

    @Around("businessMethods()")
    public Object log (ProceedingJoinPoint call) throws Throwable {
        try {
            return call.proceed();
        } finally {
            Object[] args = call.getArgs();
            System.out.println(String.format("ASPECT.LOGGER: '%s' called with args '%s'!",call
                    .toShortString(), Arrays.deepToString(args)));

        }
    }
    @Pointcut("execution(public * ru.ivan.linkss.service.LinksServiceImpl.*(..))" +
            "&& !checkFreeLinks() && !updateFreeLinks() && !deleteExpiredUserLinks()")
    public void businessMethods() { }

    @Pointcut("execution(* ru.ivan.linkss.service.LinksServiceImpl" +
            ".checkFreeLinks*(..))")
    private void checkFreeLinks() {}

    @Pointcut("execution(* ru.ivan.linkss.service.LinksServiceImpl" +
            ".updateFreeLinks*(..))")
    private void updateFreeLinks() {}

    @Pointcut("execution(* ru.ivan.linkss.service.LinksServiceImpl.deleteExpiredUserLinks*(..))")
    private void deleteExpiredUserLinks() {}

}
