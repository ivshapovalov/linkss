package ru.ivan.linkss.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.entity.Domain;
import ru.ivan.linkss.repository.entity.FullLink;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.List;

@Component
public class LinksServiceImpl implements LinksService {

    @Autowired
    @Qualifier(value = "repositoryTwo")
    private LinkRepository repository;

    public LinksServiceImpl() {
    }

    @Override
    public long getDBLinksSize() {
        return repository.getDBLinksSize();
    }

    @Override
    public long getDBFreeLinksSize() {
        return repository.getDBFreeLinksSize();
    }

    @Override
    public long getDomainsSize(User autorizedUser) {
        return repository.getDomainsSize(autorizedUser);
    }

    public long getUsersSize(User autorizedUser) {
        return repository.getUsersSize(autorizedUser);
    }

    public void setRepository(LinkRepository repository) {
        this.repository = repository;
    }

    @Override
    public BigInteger updateFreeLinks() {
        return repository.checkFreeLinksDB();
    }

    @Override
    public void downloadImageFromS3(String filePath, String key) {
        Util.downloadImageFromS3(filePath, key);
    }

    @Override
    public void uploadImage(String imagePath, String shortLink, String fullShortLink) {
        try {
            createQRImage(imagePath, shortLink, fullShortLink);
            sendFileToS3(imagePath, shortLink);

        } catch (IOException | WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkUser(User user) {
        return repository.checkUser(user);
    }

    @Override
    public String getRandomShortLink() {
        return repository.getRandomShortLink();
    }

    @Override
    public String createShortLink(User autorizedUser, String link) {

        return repository.createShortLink(autorizedUser, link);
    }

    @Override
    public void createUser(String userName, String password) {
        repository.createUser(userName, password);
    }

    @Override
    public List<User> getUsers(int offset, int recordsOnPage) {
        return repository.getUsers(offset, recordsOnPage);
    }

    @Override
    public User getUser(User autorizedUser, String userName) {
        return repository.getUser(autorizedUser, userName);
    }

    @Override
    public void deleteUser(User autorizedUser, String userName) {
        repository.deleteUser(autorizedUser, userName);
    }

    @Override
    public void updateUser(User autorizedUser, User newUser, User oldUser) {
        repository.updateUser(autorizedUser, newUser, oldUser);
    }

    @Override
    public String getLink(String shortLink) {
        return repository.getLink(shortLink);
    }

    @Override
    public String visitLink(String shortLink) {
        return repository.visitLink(shortLink);
    }

    @Override
    public FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath) {
        return repository.getFullLink(autorizedUser, shortLink, owner, contextPath);
    }

    @Override
    public long getLinkDays(String shortLink) {
        return repository.getLinkDays(shortLink);
    }

    @Override
    public void deleteUserLink(User user, String shortLink, String owner) {
        repository.deleteUserLink(user, shortLink, owner);
    }

    @Override
    public List<Domain> getShortStat(int offset, int recordsOnPage) {
        return repository.getShortStat(offset, recordsOnPage);
    }

    @Override
    public List<FullLink> getFullStat(String contextPath, int offset, int recordsOnPage) {
        return repository.getFullStat(contextPath, offset, recordsOnPage);
    }

    @Override
    public List<FullLink> getFullStat(String userName, String contextPath, int offset, int recordsOnPage) {
        return repository.getFullStat(userName, contextPath, offset, recordsOnPage);
    }

    @Override
    public long getUserLinksSize(User autorizedUser, String owner) {
        return repository.getUserLinksSize(autorizedUser, owner);
    }

    @Override
    public void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) {
        repository.updateLink(autorizedUser, oldFullLink, newFullLink);
    }

    @Override
    public void sendFileToS3(String imagePath, String key) {

        final AmazonS3 s3 = new AmazonS3Client();
        try {
            File file = new File(imagePath);
            s3.putObject(System.getenv("S3_BUCKET_NAME"), key, file);
        } catch (AmazonServiceException e) {
            System.err.println(e);
        }

//        //String url = System.getenv("EASYSMS_URL")+"/messages";
//        String url = "https://s3-bucket.s3.amazonaws.com/whydt";
//        String text = "{\"to\":\"+79266948741\",\"body\":\"Hello from Easy SMS Add-on for Heroku.\"}";
//        HttpClient client = new DefaultHttpClient();
//        HttpPost post = new HttpPost(url);
//        MultipartEntity entity = new MultipartEntity();
//        entity.addPart("file", new FileBody(new File(fileName)));
//
//        post.setHeader("Content-Type", "text/html; charset=UTF-8");
//        List<NameValuePair> urlParameters = new ArrayList<>();
//        urlParameters.add(new BasicNameValuePair("key", "uploads/${" + fileName + "}"));
//        urlParameters.add(new BasicNameValuePair("AWSAccessKeyId", "AKIAIAZ7FWJQR56IJ6XQ"));
//        urlParameters.add(new BasicNameValuePair("acl", "public-read"));
//        urlParameters.add(new BasicNameValuePair("policy", "YOUR_POLICY_DOCUMENT_BASE64_ENCODED"));
//        urlParameters.add(new BasicNameValuePair("signature", "YOUR_POLICY_DOCUMENT_BASE64_ENCODED"));
//        urlParameters.add(new BasicNameValuePair("Content-Type", "image/png"));
//        urlParameters.add(new BasicNameValuePair("Content-Type", "image/png"));
//
//        try {
//            post.setEntity(new UrlEncodedFormEntity(urlParameters));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            HttpResponse response = client.execute(post);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void createQRImage(String filePath, String shortLink, String fullShortLink) throws
            WriterException,
            IOException {

        int size = 125;
        String fileType = "png";
        File qrFile = new File(filePath);

        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable hintMap = new Hashtable();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(fullShortLink,
                BarcodeFormat.QR_CODE, size, size, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,
                BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        ImageIO.write(image, fileType, qrFile);
    }
}
