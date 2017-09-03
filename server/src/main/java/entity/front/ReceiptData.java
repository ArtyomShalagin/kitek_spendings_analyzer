package entity.front;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cat on 02.09.17.
 */
public class ReceiptData {
    public List<ReceiptItem> items = new ArrayList<>();

    public static class ReceiptItem {
        public Integer quantity;
        public String name;
        public Integer cost_one;
        public Integer sum;
    }
}
