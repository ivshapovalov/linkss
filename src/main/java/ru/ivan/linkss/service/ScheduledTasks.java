package ru.ivan.linkss.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {

    @Autowired
    private LinkssService service;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void updateFreeLinks() {
//        System.out.println(String.format("The time is now %s. Check free links count",
//                dateFormat.format(new Date())));
        BigInteger addedKeys = service.updateFreeLinks();
        if (!addedKeys.equals(BigInteger.ZERO)) {
            System.out.println(String.format("The time is now %s. The %s key added in free link " +
                    "db ",dateFormat.format(new Date()), addedKeys));
        } else {
            //System.out.println("There is no need to increase free links value");
        }
    }
}
