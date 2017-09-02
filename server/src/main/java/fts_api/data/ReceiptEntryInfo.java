package fts_api.data;

public class ReceiptEntryInfo {
    public final String name;
    public final int price;
    public final int quantity;
    public int category = -1;

    public ReceiptEntryInfo(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReceiptEntryInfo that = (ReceiptEntryInfo) o;

        if (price != that.price) return false;
        if (quantity != that.quantity) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + price;
        result = 31 * result + quantity;
        return result;
    }

    @Override
    public String toString() {
        return "ReceiptEntryInfo{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
