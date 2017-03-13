package ru.ivan.linkss.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.entity.Domain;
import ru.ivan.linkss.repository.entity.FullLink;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.repository.entity.UserDTO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

@Service
@Qualifier(value = "service")
public class LinksServiceImpl implements LinksService {

    private static final int SIZE_OF_POOL = 15;
    private static final String IMAGE_EXTENSION = "png";
    private static final String FILE_SEPARTOR = File.separator;
    private static final String QR_FOLDER = "resources"+ FILE_SEPARTOR +"qr";

    @Autowired
    @Qualifier(value = "repositoryOne")
    private LinkRepository repository;

    public LinksServiceImpl() {
    }

    @Override
    public void clear(String path) {
        repository.clear();
        deleteAllImages(path);
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
    public BigInteger updateFreeLinks() throws Exception {
        return repository.checkFreeLinksDB();
    }

    @Override
    public BigInteger deleteExpiredUserLinks() throws Exception {
        return repository.deleteExpiredUserLinks();
    }

    @Override
    public User checkUser(User user) {
        return repository.checkUser(user);
    }

    @Override
    public String getRandomShortLink() {
        return repository.getRandomShortLink();
    }

    @Override
    public String createShortLink(User autorizedUser, String link, String path, String context) {

        String shortLink = repository.createShortLink(autorizedUser, link);
        if (shortLink != null) {
            String imagePath = path + "resources" + FILE_SEPARTOR +"qr"+ FILE_SEPARTOR + shortLink +
                    "." + IMAGE_EXTENSION;
            String shortLinkPath = context + shortLink;

            try {
                createQRImage(imagePath, shortLink, shortLinkPath);
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //uploadImageToFTP(imagePath, shortLink);

        }
        return shortLink;
    }

    @Override
    public void createUser(User user) {
        repository.createUser(user);
    }

    @Override
    public List<UserDTO> getUsersDTO(int offset, int recordsOnPage) {
        return repository.getUsersDTO(offset, recordsOnPage);
    }

    @Override
    public List<String> getFreeLinks(int offset, int recordsOnPage) {
        return repository.getFreeLinks(offset, recordsOnPage);
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
    public void clearUser(User autorizedUser, String userName) {
        repository.clearUser(autorizedUser, userName);
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
    public long getLinkExpirePeriod(String shortLink) {
        return repository.getLinkExpirePeriod(shortLink);
    }

    @Override
    public void deleteUserLink(User user, String shortLink, String owner) {
        repository.deleteLink(user, shortLink, owner);
    }

    @Override
    public void deleteArchiveLink(User user, String shortLink, String owner, String path) {
        repository.deleteArchiveLink(user, shortLink, owner);
        String imagePath = path + "resources" + FILE_SEPARTOR + shortLink + "." + IMAGE_EXTENSION;

        deleteImage(path, shortLink);
    }

    @Override
    public void restoreArchiveLink(User user, String shortLink, String owner) {
        repository.restoreArchiveLink(user, shortLink, owner);
    }

    @Override
    public void deleteFreeLink(String shortLink) {
        repository.deleteFreeLink(shortLink);
    }

    @Override
    public List<Domain> getShortStat(int offset, int recordsOnPage) {
        return repository.getShortStat(offset, recordsOnPage);
    }

    @Override
    public List<FullLink> getUserLinks(String userName, String contextPath, int offset, int recordsOnPage) {
        return repository.getUserLinks(userName, contextPath, offset, recordsOnPage);
    }

    @Override
    public List<FullLink> getUserArchive(String userName, String contextPath, int offset, int
            recordsOnPage) {
        return repository.getUserArchive(userName, contextPath, offset, recordsOnPage);
    }

    @Override
    public long getUserLinksSize(User autorizedUser, String owner) {
        return repository.getUserLinksSize(autorizedUser, owner);
    }

    @Override
    public long getUserArchiveSize(User autorizedUser, String owner) {
        return repository.getUserArchiveSize(autorizedUser, owner);
    }

    @Override
    public void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) {
        repository.updateLink(autorizedUser, oldFullLink, newFullLink);
    }

    private void deleteImage(String path, String key) {

        String filePath = path + "resources" + FILE_SEPARTOR + key + "." + IMAGE_EXTENSION;
        File imageFile = new File(filePath);
        imageFile.delete();

    }

    @Override
    public void deleteAllImages(String path) {

        deleteLocalImages(path+QR_FOLDER);

    }

    @Override
    public BigInteger deleteLocalImages(String path) {

        //local
        File directory = new File(path);
        File[] files = directory.listFiles();
        final BigInteger[] deletedFiles = {BigInteger.ZERO};
        if (files != null && files.length != 0) {
            Arrays.asList(files).forEach(file -> {
                if (file.isFile()) file.delete();
                deletedFiles[0] = deletedFiles[0].add(BigInteger.ONE);
            });
        }
        return deletedFiles[0];

    }

    @Override
    public void createQRImage(String filePath, String shortLink, String fullShortLink) throws
            WriterException,
            IOException {

        int size = 125;
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
        ImageIO.write(image, IMAGE_EXTENSION, qrFile);
    }
}
