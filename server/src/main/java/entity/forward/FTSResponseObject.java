package entity.forward;

import java.util.List;

/**
 * Created by cat on 02.09.17.
 */
public class FTSResponseObject {
    public Document document;

    public static class Document {
        public Receipt receipt;
    }

    public static class Example {
        public Document document;
    }


    public static class Item {
        public Integer price;
        public Integer nds18;
        public String barcode;
        public List<Object> modifiers = null;
        public Integer sum;
        public Integer quantity;
        public String name;
    }

    public static class Receipt {
        public String user;
        public Double fiscalSign;
        public List<Object> stornoItems = null;
        public Integer totalSum;
        public List<Item> items = null;
        public Integer ecashTotalSum;
        public Integer requestNumber;
        public String operator;
        public String fiscalDriveNumber;
        public Integer cashTotalSum;
        public Integer operationType;
        public Integer nds18;
        public Integer fiscalDocumentNumber;
        public String kktRegId;
        public Integer taxationType;
        public Integer shiftNumber;
        public List<Object> modifiers = null;
        public Integer receiptCode;
        public String dateTime;
        public String userInn;
        public String rawData;
    }
}