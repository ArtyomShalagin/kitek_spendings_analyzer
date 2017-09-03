package dataflow;

import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User data is now stored in DATA_DIR/username/
 * This will probably be changed to normal databases
 */
public class DataManager {
    private static final String DATA_DIR = "user_data/";
    private static ConcurrentHashMap<String, Object> syncTokens = new ConcurrentHashMap<>();

    private DataManager() { }

    private static CellProcessor[] getProcessors() {
        // Category Name Cost Date DayOfWeek

        return new CellProcessor[] {
                new NotNull(), // category
                new NotNull(), // name
//                new Optional(new ParseInt()), // cost
                new NotNull(), // cost
//                new ParseDate("yyyy/dd/MM"), // date
                new NotNull(), // date
                new NotNull(), // day of week
        };
    }

    private static String getPath(String username) {
        return DATA_DIR + username + ".csv";
    }

    private static Object getSyncToken(String username) {
        syncTokens.putIfAbsent(username, new Object());
        return syncTokens.get(username);
    }

    private static void createFileIfAbsent(String filepath) throws IOException {
        File file = Paths.get(filepath).toFile();
        if (file.exists()) {
            return;
        }
        File parent = file.getParentFile();
        if (parent != null) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
    }

    public static List<EntryBean> getUserData(String username) throws IOException {
        List<EntryBean> data = new ArrayList<>();
        synchronized (getSyncToken(username)) {
            ICsvBeanReader beanReader = null;
            try {
                if (!Paths.get(getPath(username)).toFile().exists()) {
                    return new ArrayList<>();
                }
                beanReader = new CsvBeanReader(new FileReader(getPath(username)), CsvPreference.STANDARD_PREFERENCE);

                // the header elements are used to map the values to the bean (names must match)
                final String[] header = beanReader.getHeader(true);
                final CellProcessor[] processors = getProcessors();

                if (header == null) { // file is empty
                    return new ArrayList<>();
                }

                EntryBean entry;
                while ((entry = beanReader.read(EntryBean.class, header, processors)) != null) {
                    data.add(entry);
                }
            } finally {
                if (beanReader != null) {
                    beanReader.close();
                }
            }
        }
        return data;
    }

    public static void appendUserData(String username, List<EntryBean> newData) throws IOException {
        createFileIfAbsent(getPath(username));
        List<EntryBean> data = getUserData(username);
        data.addAll(newData);
        ICsvBeanWriter beanWriter = null;
        try {
            beanWriter = new CsvBeanWriter(new FileWriter(getPath(username)),
                    CsvPreference.STANDARD_PREFERENCE);

            final String[] header = { "category", "name", "cost", "date", "dayOfWeek" };
            final CellProcessor[] processors = getProcessors();

            beanWriter.writeHeader(header);

            for (final EntryBean entry : data) {
                beanWriter.write(entry, header, processors);
            }
        } finally {
            if (beanWriter != null) {
                beanWriter.close();
            }
        }
    }
}
