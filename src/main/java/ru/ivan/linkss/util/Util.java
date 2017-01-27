package ru.ivan.linkss.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
            System.out.println("Ошибка:" +url);

            List<Integer> list1=new ArrayList<>();
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
}
