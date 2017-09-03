package entity.front;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cat on 03.09.17.
 */
public class CatValue {
    public List<CatValuePair> result = new ArrayList<>();

    /**
     * category-value pair
     * im not going to put jackson annotations onto util.Pair
     */
    public static class CatValuePair {
        public String category;
        public Integer value;

        public CatValuePair() {
        }

        public CatValuePair(String category, Integer value) {
            this.category = category;
            this.value = value;
        }
    }
}
