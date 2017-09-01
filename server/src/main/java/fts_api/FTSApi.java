package fts_api;

import util.IOUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FTSApi {
    private static final Map<String, String> headers;

    private static final String PASSWORD_KEY = "password";
    private static final String URL_TEMPLATE_KEY = "url_template";

    static {
        headers = new HashMap<String, String>() {{
            put("User-Agent", "okhttp/3.0.1");
            put("Device-Id", "748036d688ec41c5");
            put("Device-OS", "Adnroid 7.1.1");
            put("Version", "2");
            put("ClientVersion", "1.4.2");
            put("Host", "proverkacheka.nalog.ru:8888");
            put("Connection", "close");
        }};
    }

    private static String buildUrl(String fiscalSign) {
        String urlTemplate = FTSProperties.getInstance().getProperty(URL_TEMPLATE_KEY);
        return String.format(urlTemplate, fiscalSign);
    }

    public static FTSResult requestReceiptInfo(String fiscalSign) throws IOException {
        String urlString = buildUrl(fiscalSign);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        headers.forEach(connection::setRequestProperty);
        String password = FTSProperties.getInstance().getProperty(PASSWORD_KEY);
        connection.addRequestProperty("Authorization", "Basic " + password);

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
