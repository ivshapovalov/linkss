package ru.ivan.linkss.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.service.LinksService;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {

    @Autowired
    private LinksService service;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 1000)
    public void updateFreeLinks() {
        long startTime = System.nanoTime();
        BigInteger addedKeys = service.updateFreeLinks();
        if (!BigInteger.ZERO.equals(addedKeys)) {
            long endTime = System.nanoTime();

            System.out.println(String.format("%s. Free link DB updated by %s keys in %s seconds",
                    dateFormat.format(new Date()), addedKeys,(endTime -
                            startTime) / 1000000000));
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkExpiredUserLinks() {
        long startTime = System.nanoTime();
        BigInteger deletedKeys = service.deleteExpiredUserLinks();
        if (!BigInteger.ZERO.equals(deletedKeys)) {
            long endTime = System.nanoTime();
            System.out.println(String.format("%s. Delete %s expired keys in %s seconds",
                    dateFormat.format(new Date()), deletedKeys,(endTime -
                            startTime) / 1000000000));
        }
    }
}
