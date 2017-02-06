package ru.ivan.linkss.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


public class SimpleProfiler {

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
}
