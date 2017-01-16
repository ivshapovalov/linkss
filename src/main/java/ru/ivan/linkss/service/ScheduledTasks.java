package ru.ivan.linkss.service;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Autowired
    private LinkssService service;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        if (!service.updateFreeLinksInProgress()) {
            BigInteger addedKeys = service.updateFreeLinks();
            log.info("The time is now {}", dateFormat.format(new Date()));
            log.info("The {} key added in free link db ", addedKeys);
        }
    }
}
