package ru.ivan.linkss.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.RepositoryException;
import ru.ivan.linkss.repository.entity.*;
import ru.ivan.linkss.util.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Service
@Qualifier(value = "service")
public class LinksServiceImpl implements LinksService {

    private static final int SIZE_OF_POOL = 15;
    private static final String IMAGE_EXTENSION = "png";
    private static final String FILE_SEPARTOR = File.separator;
    private static final String QR_FOLDER = "resources" + FILE_SEPARTOR + "qr";
    private static final String PARAM_IP = "ip";

    @Autowired
    @Qualifier(value = "repositoryOne")
    private LinkRepository repository;

    @Autowired
    @Qualifier(value = "mail")
    private Mail mail;

    public LinksServiceImpl() {
    }

    @Override
    public void sendMail(User user, String path) {
        String UUID = repository.generateNewUUID(user);
        String verifyURL = path + UUID;
        mail.send(user, verifyURL);
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
    public long getDomainsSize(User autorizedUser) throws RepositoryException {
        return repository.getDomainsSize(autorizedUser);
    }

    public long getUsersSize(User autorizedUser) throws RepositoryException {
        return repository.getUsersSize(autorizedUser);
    }

    public long getVisitsActualSize(User autorizedUser) throws RepositoryException {
        return repository.getVisitsByDomainActualSize(autorizedUser);
    }

    public long getVisitsHistorySize(User autorizedUser) throws RepositoryException {
        return repository.getVisitsByDomainHistorySize(autorizedUser);
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
    public User checkUser(User user) throws RepositoryException {
        return repository.checkUser(user);
    }

    @Override
    public boolean checkLinkOwner(String key, String owner) {
        return repository.checkLinkOwner(key, owner);
    }

    @Override
    public String getRandomShortLink() {
        return repository.getRandomShortLink();
    }

    @Override
    public String createShortLink(User autorizedUser, String link, String path, String context,
                                  Map<String, String> params) throws RepositoryException {

        String ip = params.get(PARAM_IP);
        IpPosition ipPosition = getPosition(ip);
        if (ipPosition == null) {
            ipPosition = new IpPosition(ip);
        }
        String shortLink = repository.createShortLink(autorizedUser, link, ipPosition);
        if (shortLink != null) {
            String imagePath = path + "resources" + FILE_SEPARTOR + "qr" + FILE_SEPARTOR + shortLink +
                    "." + IMAGE_EXTENSION;
            String shortLinkPath = context + shortLink;

            try {
                createQRImage(imagePath, shortLink, shortLinkPath);
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return shortLink;
    }

    @Override
    public void createUser(User user,Map<String,String> params) throws RepositoryException {
        String ip = params.get(PARAM_IP);
        IpPosition ipPosition = getPosition(ip);
        if (ipPosition == null) {
            ipPosition = new IpPosition(ip);
        }
        user.setIpPosition(ipPosition);
        repository.createUser(user);
    }

    @Override
    public String verifyUser(String uuid) {
        return repository.verifyUser(uuid);
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
    public User getUser(User autorizedUser, String userName) throws RepositoryException {
        return repository.getUser(autorizedUser, userName);
    }

    @Override
    public void deleteUser(User autorizedUser, String userName) throws RepositoryException {
        repository.deleteUser(autorizedUser, userName);
    }

    @Override
    public void clearUser(User autorizedUser, String userName) throws RepositoryException {
        repository.clearUser(autorizedUser, userName);
    }

    @Override
    public void updateUser(User autorizedUser, User newUser, User oldUser) throws RepositoryException {
        repository.updateUser(autorizedUser, newUser, oldUser);
    }

    @Override
    public Link getLink(String shortLink) {
        return repository.getLink(shortLink);
    }

    @Override
    public String visitLink(String shortLink, Map<String, String> params) {

        String ip = params.get(PARAM_IP);
        IpPosition ipPosition = getPosition(ip);
        if (ipPosition == null) {
            ipPosition = new IpPosition(ip);
        }
        return repository.visitLink(shortLink, ipPosition, params);
    }

    @Override
    public String visitLinkwithIpChecking(String shortLink, Map<String, String> params) {

        String ip = params.get(PARAM_IP);
        IpPosition ipPosition = getPosition(ip);
        if (ipPosition == null) {
            return null;
        } else {
            return repository.visitLink(shortLink, ipPosition, params);
        }
    }

    private IpPosition getPosition(String ip) {

        try (CloseableHttpClient geoHTTPClient = HttpClientBuilder.create().build()) {

            String url = Constants.GEOIP_URL;
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(url)
                    .addParameter("ip", ip)
                    .addParameter("key", Constants.GEOIP_KEY)
                    .build();
            HttpGet request = new HttpGet(uri);
            request.addHeader("accept", "application/json");
            HttpResponse response = geoHTTPClient.execute(request);

            IpPosition position = null;
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            String jsonPosition;
            if ((jsonPosition = br.readLine()) != null) {
                try {
                    position = new ObjectMapper().readValue(jsonPosition, IpPosition
                            .class);
                    return position;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath) throws RepositoryException {
        return repository.getFullLink(autorizedUser, shortLink, owner, contextPath);
    }

    @Override
    public long getLinkExpirePeriod(String shortLink) {
        return repository.getLinkExpirePeriod(shortLink);
    }

    @Override
    public void deleteUserLink(User user, String shortLink, String owner) throws RepositoryException {
        repository.deleteLink(user, shortLink, owner);
    }

    @Override
    public void deleteLinkVisit(User user, String owner, String key, String time) throws RepositoryException {
        repository.deleteVisit(user, owner, key, time);
    }

    @Override
    public void deleteArchiveLink(User user, String shortLink, String owner, String path) throws RepositoryException {
        repository.deleteArchiveLink(user, shortLink, owner);
        deleteImage(path, shortLink);
    }

    @Override
    public void restoreArchiveLink(User user, String shortLink, String owner) throws RepositoryException {
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
    public List<Visit> getLinkVisits(User autorizedUser, String owner, String key, int
            offset, long
                                             recordsOnPage) throws RepositoryException {
        return repository.getLinkVisits(autorizedUser, owner, key, offset,
                recordsOnPage);
    }
    @Override
    public List<Visit> getDomainActualVisits(User autorizedUser, String key, int
            offset, long
                                             recordsOnPage) throws RepositoryException {
        return repository.getDomainActualVisits(autorizedUser, key, offset,
                recordsOnPage);
    }
    @Override
    public List<Visit> getDomainHistoryVisits(User autorizedUser, String key, int
            offset, long
                                             recordsOnPage) throws RepositoryException {
        return repository.getDomainHistoryVisits(autorizedUser, key, offset,
                recordsOnPage);
    }

    @Override
    public List<Visit> getUserVisits(User autorizedUser, String owner) {
        return repository.getUserVisits(autorizedUser, owner);
    }

    @Override
    public List<Visit> getAllVisits() {
        return repository.getAllVisits();
    }

    @Override
    public List<Link> getAllLinks() {
        return repository.getAllLinks();
    }

    @Override
    public List<FullLink> getUserArchive(String userName, String contextPath, int offset, int
            recordsOnPage) {
        return repository.getUserArchive(userName, contextPath, offset, recordsOnPage);
    }

    @Override
    public long getUserLinksSize(User autorizedUser, String owner) throws RepositoryException {
        return repository.getUserLinksSize(autorizedUser, owner);
    }

    @Override
    public long getUserArchiveSize(User autorizedUser, String owner) throws RepositoryException {
        return repository.getUserArchiveSize(autorizedUser, owner);
    }

    @Override
    public long getLinkVisitsSize(User autorizedUser, String owner, String key) throws RepositoryException {
        return repository.getLinkVisitsSize(autorizedUser, owner, key);
    }
    @Override
    public long getDomainActualVisitsSize(User autorizedUser, String key) throws
            RepositoryException {
        return repository.getDomainActualVisitsSize(autorizedUser, key);
    }
    @Override
    public long getDomainHistoryVisitsSize(User autorizedUser, String key) throws
            RepositoryException {
        return repository.getDomainHistoryVisitsSize(autorizedUser, key);
    }

    @Override
    public long getUserVisitsSize(User autorizedUser, String owner) {
        return repository.getUserVisitsSize(autorizedUser, owner);
    }

    @Override
    public void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) throws RepositoryException {
        repository.updateLink(autorizedUser, oldFullLink, newFullLink);
    }

    private void deleteImage(String path, String key) {

        String filePath = path + "resources" + FILE_SEPARTOR + key + "." + IMAGE_EXTENSION;
        File imageFile = new File(filePath);
        imageFile.delete();

    }

    @Override
    public void deleteAllImages(String path) {

        deleteLocalImages(path + QR_FOLDER);

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
