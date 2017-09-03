package fts_api.data;

/**
 * Created by cat on 03.09.17.
 */
public class FTSResult {
    private final int responseCode;
    private final String data;

    private final int VALID_RESPONSE_CODE = 200;

    public FTSResult(int responseCode, String data) {
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
