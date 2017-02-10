package ru.ivan.linkss.util;

import com.lambdaworks.redis.RedisClient;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.*;

public class FTPManager {

    FTPClient ftp = null;

    public FTPManager() throws Exception {
        this(System.getenv("FTP_HOST"), System.getenv("FTP_LOGIN"),
                System.getenv("FTP_PASSWORD"));
    }

    public FTPManager(String host, String user, String pwd) throws Exception {
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.enterLocalPassiveMode();
        ftp.connect(host);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(user, pwd);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }

    public void downloadFile(String localFilePath, String key) {
        try (FileOutputStream fos = new FileOutputStream(key+".png")) {
            this.ftp.retrieveFile(localFilePath, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(String localFilePath, String key) {

        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            File firstLocalFile = new File(localFilePath);
            String firstRemoteFile = key+".png";
            InputStream inputStream = new FileInputStream(firstLocalFile);
            boolean done = ftp.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (IOException f) {
                // do nothing as file is already downloaded from FTP server
            }
        }
    }
}