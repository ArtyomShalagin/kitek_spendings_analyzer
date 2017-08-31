package fts_api;

import util.IOUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FTSApi {
    private static final String password;
    private static final Map<String, String> headers;

    private static final String URL_TEMPLATE = "http://proverkacheka.nalog.ru:8888/v1/inns/*/kkts/*/fss/8710000101053109/tickets/16308?fiscalSign=%s&sendToEmail=no";
    private static final String PROPERTIES_FILE_NAME = "fts.properties";

    static {
        String passwordValue = "";

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(PROPERTIES_FILE_NAME));
            if (!properties.containsKey("password")) {
                System.err.println("No fts password in " + PROPERTIES_FILE_NAME);
            } else {
                passwordValue = properties.getProperty("password");
            }
        } catch (IOException e) {
            System.err.println("Unable to find properties file " + PROPERTIES_FILE_NAME);
        }

        password = passwordValue;

        headers = new HashMap<String, String>() {{
            put("User-Agent", "okhttp/3.0.1");
            put("Authorization", "Basic " + password);
            put("Device-Id", "748036d688ec41c5");
            put("Device-OS", "Adnroid 7.1.1");
            put("Version", "2");
            put("ClientVersion", "1.4.2");
            put("Host", "proverkacheka.nalog.ru:8888");
            put("Connection", "close");
        }};
    }

    private static String buildUrl(String fiscalSign) {
        return String.format(URL_TEMPLATE, fiscalSign);
    }

    public static FTSResult requestReceiptInfo(String fiscalSign) throws IOException {
        String urlString = buildUrl(fiscalSign);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        headers.forEach(connection::setRequestProperty);

        int responseCode = connection.getResponseCode();
        String data = IOUtil.readFully(connection.getInputStream());

        return new FTSResult(responseCode, data);
    }

    public static class FTSResult {
        private final int responseCode;
        private final String data;

        private final int VALID_RESPONSE_CODE = 200;

        FTSResult(int responseCode, String data) {
            this.responseCode = responseCode;
            this.data = data;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getData() {
            return data;
        }

        public boolean isValid() {
            return responseCode == VALID_RESPONSE_CODE;
        }
    }

}
