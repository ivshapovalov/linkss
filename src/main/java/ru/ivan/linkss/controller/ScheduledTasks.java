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
    LinksService service;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 1000)
    public void updateFreeLinks() {
        long startTime = System.nanoTime();
        try {
            BigInteger addedKeys = service.updateFreeLinks();
            if (!BigInteger.ZERO.equals(addedKeys)) {
                long endTime = System.nanoTime();

                System.out.println(String.format("%s. Free link DB updated by %s keys in %s seconds",
                        dateFormat.format(new Date()), addedKeys, (endTime -
                                startTime) / 1000000000));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkExpiredUserLinks() {

        try {
            long startTime = System.nanoTime();
            BigInteger deletedKeys = service.deleteExpiredUserLinks();
            if (!BigInteger.ZERO.equals(deletedKeys)) {
                long endTime = System.nanoTime();
                System.out.println(String.format("%s. Delete %s expired keys in %s seconds",
                        dateFormat.format(new Date()), deletedKeys, (endTime -
                                startTime) / 1000000000));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 1 * *  ?")
//    @Scheduled(fixedRate = 5000)
    public void deleteQrImages() {

        try {
            long startTime = System.nanoTime();
            String path = getClass().getClassLoader().getResource("..//../resources//qr")
                    .getPath();
            BigInteger deletedImages = service.deleteLocalImages(path);
            if (!BigInteger.ZERO.equals(deletedImages)) {
                long endTime = System.nanoTime();
                System.out.println(String.format("%s. Delete %s qr images in %s seconds",
                        dateFormat.format(new Date()), deletedImages, (endTime -
                                startTime) / 1000000000));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
