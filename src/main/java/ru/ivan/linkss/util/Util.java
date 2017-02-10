package ru.ivan.linkss.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static String getDomainName(String url) throws NullPointerException {
        URI uri = null;
        try {
            if (url.contains("://")) {
                uri = new URI(url);
            } else {
                uri = new URI("http://" + url);
            }
            String domain = uri.getHost();
            if (domain==null) domain="";
            return domain.startsWith("www") ? domain.substring(domain.indexOf(".")+1) : domain;
        } catch (URISyntaxException e) {
            //System.out.println("Ошибка:" +url);
            return "";
        }
    }
    public static void downloadImageFromS3(String filePath, String key) {

        AmazonS3 s3 = new AmazonS3Client();
        try {
            String bucketName=System.getenv("S3_BUCKET_NAME");
            S3Object o = s3.getObject(bucketName, key);
            S3ObjectInputStream s3is = o.getObjectContent();
            File file = new File(filePath);

            FileOutputStream fos = new FileOutputStream(file);
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String convertSecondsToPeriod(long allSeconds) {
        long days = allSeconds / 3600 / 24;
        long hours = (allSeconds % (3600 * 24)) / 3600;
        long minutes = (allSeconds % 3600) / 60;
        long seconds = allSeconds % 60;

        String timeString = String.format("%03d:%02d:%02d:%02d", days, hours, minutes, seconds);
        return timeString;
    }
    public static String convertLocalDateTimeToString(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = localDateTime.format(formatter);
        return formattedDateTime;
    }

    public static long convertPeriodToSeconds(String period) {

        if ("".equals(period)) {
            return 0;
        }
        String[] parts= period.split(":");
        int length=parts.length;
        if (length==0) {
            throw new RuntimeException(String.format("Period must be in dd:hh:mm:ss format, " +
                    "but now " +
                    "'%s'!" +
                    " Try again!",period));
        }

        long allSeconds=0;
        try {
            int seconds = Integer.parseInt(parts[parts.length - 1]);
            int minutes=0,hours=0,days=0;
            if (length > 1) {
                minutes = Integer.parseInt(parts[parts.length - 2]);
            }
            if (length > 2) {
                hours = Integer.parseInt(parts[parts.length - 3]);
            }
            if (length > 3) {
                days = Integer.parseInt(parts[parts.length - 4]);
            }
            allSeconds= days*3600*24+hours*3600+minutes*60+seconds;
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Period must be in dd:hh:mm:ss format, but " +
                    "now " +
                    "'%s'!" +
                    " Try again!",period));        }
        return allSeconds;
    }
}
