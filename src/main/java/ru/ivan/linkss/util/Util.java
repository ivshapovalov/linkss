package ru.ivan.linkss.util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public static void downloadImageFromFTP(String filePath, String key) {

        FTPManager ftpManager =
                null;
        try {
            ftpManager = new FTPManager(System.getenv("FTP_HOST"), System.getenv("FTP_LOGIN"),
                    System.getenv("FTP_PASSWORD"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ftpManager.downloadFile(filePath,key);
        ftpManager.disconnect();
    }
    public static void uploadImageToFTP(String filePath, String key) {

        FTPManager ftpManager =
                null;
        try {
            ftpManager = new FTPManager(System.getenv("FTP_HOST"), System.getenv("FTP_LOGIN"),
                    System.getenv("FTP_PASSWORD"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ftpManager.uploadFile(filePath,key);
        ftpManager.disconnect();

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
