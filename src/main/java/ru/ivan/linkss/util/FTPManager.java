package ru.ivan.linkss.util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class FTPManager {

    private static final String IMAGE_EXTENSION = "png";
    FTPClient ftp = null;

    public FTPManager() throws Exception {
        this(System.getenv("FTP_HOST"), System.getenv("FTP_LOGIN"),
                System.getenv("FTP_PASSWORD"));
    }

    public FTPManager(String host, String user, String pwd) throws Exception {
        ftp = new FTPClient();
        //ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
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
        try (FileOutputStream fos = new FileOutputStream(key + "." + IMAGE_EXTENSION)) {
            this.ftp.retrieveFile(localFilePath, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String key) {
        try {
            this.ftp.deleteFile(key + "." + IMAGE_EXTENSION);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllFiles() {
        try {
            FTPFile[] files = this.ftp.listFiles();
            if (files.length != 0) {
                Arrays.asList(files).forEach(file -> {
                    try {
                        ftp.deleteFile(file.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(String localFilePath, String key) {

        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            File firstLocalFile = new File(localFilePath);
            String firstRemoteFile = key + "." + IMAGE_EXTENSION;
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