package fts_api.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReceiptInfo {
    /*
    impl note
    jackson politely asks to keep these
    they're supposedly threadsafe, mapper is very heavy and is an afterburner cache as well
    it'd seem that you're not using it (data-binding) though, but w/e

    todo: keep object mapper from server
    todo todo: get a depinject framework.
     */
    private static JsonFactory factory = new JsonFactory();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        factory.setCodec(objectMapper);
    }

    public final int sum;
    public final String date;
    public final String time;
    public final String fiscalSign;
    public final String fiscalDocumentNumber;
    public final String fiscalDriveNumber;
    public final List<ReceiptEntryInfo> items;

    public ReceiptInfo(int sum, String date, String time, String fiscalSign, String fiscalDocumentNumber, String fiscalDriveNumber, List<ReceiptEntryInfo> items) {
        this.sum = sum;
        this.date = date;
        this.time = time;
        this.fiscalSign = fiscalSign;
        this.fiscalDocumentNumber = fiscalDocumentNumber;
        this.fiscalDriveNumber = fiscalDriveNumber;
        this.items = items;
    }

    public static ReceiptInfo parseReceiptInfo(String data) {
        try {
            JsonParser parser = factory.createParser(data);

            TreeNode root = parser.readValueAsTree();
            TreeNode receiptNode = root.path("document").path("receipt");
            int sum = ((ValueNode) receiptNode.get("totalSum")).asInt();
            String fiscalSign = ((ValueNode) receiptNode.get("totalSum")).asText();
            String fiscalDocumentNumber = ((ValueNode) receiptNode.get("fiscalDocumentNumber")).asText();
            String fiscalDriveNumber = ((ValueNode) receiptNode.get("fiscalDriveNumber")).asText();
            String datetime = ((ValueNode) receiptNode.get("dateTime")).asText();
            String[] datetimeParts = datetime.split("T");
            String date = datetimeParts[0];
            String time = datetimeParts[1];

            List<ReceiptEntryInfo> items = new ArrayList<>();
            ArrayNode itemsNode = (ArrayNode) receiptNode.path("items");
            for (JsonNode item : itemsNode) {
                String name = item.path("name").asText();
                int price = item.path("price").asInt();
                int quantity = item.path("quantity").asInt();
                items.add(new ReceiptEntryInfo(name, price, quantity));
            }

            return new ReceiptInfo(sum, date, time, fiscalSign, fiscalDocumentNumber, fiscalDriveNumber, items);
        } catch (IOException e) {
            System.err.println("Unable to parse json with ReceiptInfo: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReceiptInfo that = (ReceiptInfo) o;

        if (sum != that.sum) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (fiscalSign != null ? !fiscalSign.equals(that.fiscalSign) : that.fiscalSign != null) return false;
        if (fiscalDocumentNumber != null ? !fiscalDocumentNumber.equals(that.fiscalDocumentNumber) : that.fiscalDocumentNumber != null)
            return false;
        if (fiscalDriveNumber != null ? !fiscalDriveNumber.equals(that.fiscalDriveNumber) : that.fiscalDriveNumber != null)
            return false;
        return items != null ? items.equals(that.items) : that.items == null;
    }

    @Override
    public int hashCode() {
        int result = sum;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (fiscalSign != null ? fiscalSign.hashCode() : 0);
        result = 31 * result + (fiscalDocumentNumber != null ? fiscalDocumentNumber.hashCode() : 0);
        result = 31 * result + (fiscalDriveNumber != null ? fiscalDriveNumber.hashCode() : 0);
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReceiptInfo{" +
                "sum=" + sum +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", fiscalSign='" + fiscalSign + '\'' +
                ", fiscalDocumentNumber='" + fiscalDocumentNumber + '\'' +
                ", fiscalDriveNumber='" + fiscalDriveNumber + '\'' +
                ", items=" + items +
                '}';
    }
}
