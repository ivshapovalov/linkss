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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

@Component
public class ServiceImpl implements Service {

    @Autowired
    private LinkRepository repository;

    public ServiceImpl() {
    }

    @Override
    public String getRandomShortLink() {
        return repository.getRandomShortLink();
    }

    @Override
    public String create(String link) {

        String shortLink = repository.create(link);

        return shortLink;
    }

    @Override
    public String get(String shortLink) {
        return repository.get(shortLink);
    }

    @Override
    public List<List<String>> getShortStat() {
        return repository.getShortStat();
    }

    @Override
    public List<FullLink> getFullStat(String contextPath) {
        return repository.getFullStat(contextPath);
    }

    @Override
    public void createQRImage(String path, String link, String shortLink) throws WriterException,
            IOException {

        String filePath = path + shortLink + ".png";
        int size = 125;
        String fileType = "png";
        File qrFile = new File(filePath);

        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable hintMap = new Hashtable();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(link,
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
