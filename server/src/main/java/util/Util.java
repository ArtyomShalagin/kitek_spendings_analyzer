package util;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.*;

public class Util {
    private static Map<Integer, String> catIndexToName = new HashMap<>();
    private static Map<String, Integer> catNameToIndex = new HashMap<>();

    static {
        try {
            Scanner in = new Scanner(new File("categories.properties"));
            while (in.hasNextLine()) {
                String line = in.nextLine();
                String[] splitted = line.split("=");
                int index = Integer.parseInt(splitted[0]);
                String name = splitted[1];
                catIndexToName.put(index, name);
                catNameToIndex.put(name, index);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("Unable to read categories properties: " + e.getMessage());
        }
    }

    public static String dayOfWeekName(int index) {
        switch (index) {
            case 1:
                return "Пн";
            case 2:
                return "Вт";
            case 3:
                return "Ср";
            case 4:
                return "Чт";
            case 5:
                return "Пт";
            case 6:
                return "Сб";
            case 7:
                return "Вс";
            default:
                System.err.println("There is no day of week with index " + index);
                return "unknown";
        }
    }

    /**
     * yyyy-mm-dd
     *
     * @param date
     * @return
     */
    public static String dateToDayOfWeek(String date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(Date.valueOf(date));
        int dayOfWeekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeekIndex == 0) {
            dayOfWeekIndex = 7;
        }
        return dayOfWeekName( dayOfWeekIndex);
    }

    public static String getCurrentDateString() {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)); // possibly +1 as well?
        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }
        return year + "-" + month + "-" + day;
    }

    public static String categoryIndexToName(int index) {
        return catIndexToName.getOrDefault(index, "undefined category");
    }
}
