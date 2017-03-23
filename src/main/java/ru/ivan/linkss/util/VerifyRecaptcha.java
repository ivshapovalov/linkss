package ru.ivan.linkss.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;

import static ru.ivan.linkss.util.Constants.GOOGLE_RECAPTCHA_VERIFY_URL;
import static ru.ivan.linkss.util.Constants.GOOGLE_RECAPTCHA_VERIFY_KEY;

public class VerifyRecaptcha {

    public static boolean verify( String gRecaptchaResponse) {
        if (gRecaptchaResponse == null || gRecaptchaResponse.length() == 0) {
            return false;
        }

        try {
            URL verifyUrl = new URL(GOOGLE_RECAPTCHA_VERIFY_URL);

            HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String postParams = "secret=" + GOOGLE_RECAPTCHA_VERIFY_KEY + "&response=" + gRecaptchaResponse;

            conn.setDoOutput(true);

            OutputStream outStream = conn.getOutputStream();
            outStream.write(postParams.getBytes());

            outStream.flush();
            outStream.close();

            int responseCode = conn.getResponseCode();
            InputStream is = conn.getInputStream();

            JsonReader jsonReader = Json.createReader(is);
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            boolean success = jsonObject.getBoolean("success");
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}