package ru.ivan.linkss.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SimpleProfiler {

    @Around("businessMethods()" )
    public Object profile(ProceedingJoinPoint call) throws Throwable {
        long t0 = System.nanoTime();
        try {
            return call.proceed();
        } finally {
            long t1 = System.nanoTime();
            System.out.println("ASPECT.PROFILER: " + call.toShortString() + ", dT: " + (t1 - t0)
            );

        }
    }

    @Pointcut("execution(public * ru.ivan.linkss.repository.RedisTwoDBLinkRepositoryImpl.*(..)) " +
            "&& !checkFreeLinks() && !deleteExpiredUserLinks()")
    public void businessMethods() { }

    @Pointcut("execution(* ru.ivan.linkss.repository.RedisTwoDBLinkRepositoryImpl" +
            ".checkFreeLinks*(..))")
    private void checkFreeLinks() {}

    @Pointcut("execution(* ru.ivan.linkss.repository.RedisTwoDBLinkRepositoryImpl" +
            ".deleteExpiredUserLinks*(..))")
    private void deleteExpiredUserLinks() {}
}
