package dataflow;

public class EntryBean {
    // Category Name Cost Date DayOfWeek
    public String category;
    public String name;
    public String cost;
    public String date;
    public String dayOfWeek;

    public EntryBean() { }

    public EntryBean(String category, String name, String cost, String date, String dayOfWeek) {
        this.category = category;
        this.name = name;
        this.cost = cost;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getCost() {
        return cost;
    }

    public String getDate() {
        return date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntryBean entryBean = (EntryBean) o;

        if (category != null ? !category.equals(entryBean.category) : entryBean.category != null) return false;
        if (name != null ? !name.equals(entryBean.name) : entryBean.name != null) return false;
        if (cost != null ? !cost.equals(entryBean.cost) : entryBean.cost != null) return false;
        if (date != null ? !date.equals(entryBean.date) : entryBean.date != null) return false;
        return dayOfWeek != null ? dayOfWeek.equals(entryBean.dayOfWeek) : entryBean.dayOfWeek == null;
    }

    @Override
    public int hashCode() {
        int result = category != null ? category.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (cost != null ? cost.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (dayOfWeek != null ? dayOfWeek.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EntryBean{" +
                "category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", cost='" + cost + '\'' +
                ", date='" + date + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                '}';
    }
}
