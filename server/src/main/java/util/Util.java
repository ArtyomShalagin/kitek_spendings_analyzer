package util;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Util {
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
}
