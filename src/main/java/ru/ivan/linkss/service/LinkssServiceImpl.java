package ru.ivan.linkss.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.repository.FullLink;
import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

@Component
public class LinkssServiceImpl implements LinkssService {

    @Autowired
    private LinkRepository repository;

    public LinkssServiceImpl() {
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
    public String createShortLink(String user, String link) {

        String shortLink = repository.createShortLink(user,link);

        return shortLink;
    }

    @Override
    public void createUser(String userName, String password) {
        repository.createUser(userName,password);
    }

    @Override
    public String getLink(String shortLink) {
        return repository.getLink(shortLink);
    }

    @Override
    public List<List<String>> getShortStat() {
        return repository.getShortStat();
    }

    @Override
    public List<List<String>> getShortStat(String autorizedUser ) {
        return repository.getShortStat(autorizedUser);
    }

    @Override
    public List<FullLink> getFullStat(String contextPath) {
        return repository.getFullStat(contextPath);
    }
    @Override
    public List<FullLink> getFullStat(String userName, String contextPath) {
        return repository.getFullStat(userName, contextPath );
    }

    private void sendFileToS3(String fileName) {
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
    public void createQRImage(String path, String shortLink, String fullShortLink) throws WriterException,
            IOException {

        String filePath = path + shortLink + ".png";
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
