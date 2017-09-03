package dataflow;

import fts_api.data.ReceiptEntryInfo;
import fts_api.data.ReceiptInfo;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * User data is now stored in DATA_DIR/username/
 * This will probably be changed to normal databases
 */
public class DataManager {
    private static final String DATA_DIR = "user_data/";
    private static ConcurrentHashMap<String, Object> syncTokens = new ConcurrentHashMap<>();

    /*
    impl note
    this may get slightly outdated, but on the other hand we'll be saving time heavily.
    each append invalidates cache for the selected user
    each get may be satisfied from cache
    any modifications except value loss (gc) are made in synchronized fashion, per-user blocking, ensuring we get an at worst
        slightly outdated cache, but not an invalid one (do check pls i might be wrong)
    -- M.
    */
    private static ConcurrentHashMap<String, WeakReference<List<EntryBean>>> userDataCache = new ConcurrentHashMap<>();

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

    private static List<EntryBean> filterWith(List<EntryBean> data, Predicate<EntryBean> pred) {
        return data.parallelStream().filter(pred).collect(Collectors.toList());
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
        // attempt cache retrieval
        WeakReference<List<EntryBean>> tryCacheHandle = userDataCache.get(username);
        if (tryCacheHandle != null) { // but .?
            List<EntryBean> tryCache = tryCacheHandle.get();

            if (tryCache != null) { // but .?
                return tryCache;
            }
        }

        // cache failed, fetch directly.
        List<EntryBean> data;
        synchronized (getSyncToken(username)) {
            data = getUserDataFromFile(username);

            if (data != null)
                userDataCache.put(username, new WeakReference<>(data)); // update cache
        }

        return data;
    }

    public static void appendUserData(String username, List<EntryBean> newData) throws IOException {
        synchronized (getSyncToken(username)) {
            List<EntryBean> data = getUserDataFromFile(username);
            data.addAll(newData);

            writeToFile(getPath(username), data);
        }
    }

    public static void writeToFile(String path, List<EntryBean> data) throws IOException {
        createFileIfAbsent(path);

        ICsvBeanWriter beanWriter = null;

        try {
            beanWriter = new CsvBeanWriter(new FileWriter(path),
                    CsvPreference.STANDARD_PREFERENCE);

            final String[] header = {"category", "name", "cost", "date", "dayOfWeek"};
            final CellProcessor[] processors = getProcessors();

            beanWriter.writeHeader(header);

            for (final EntryBean entry : data) {
                beanWriter.write(entry, header, processors);
            }
        } finally {
            if (beanWriter != null) {
                beanWriter.close();
            }

            userDataCache.remove(path); // purge cache
            // will reload at next get (or put, but indirectly.
        }
    }

    public static List<EntryBean> receiptToBeans(ReceiptInfo receipt) {
        List<EntryBean> data = new ArrayList<>();
        for (ReceiptEntryInfo entryInfo : receipt.items) {
            EntryBean entry = new EntryBean(String.valueOf(entryInfo.category), entryInfo.name,
                    String.valueOf(entryInfo.price), receipt.date, Util.dateToDayOfWeek(receipt.date));
            data.add(entry);
        }
        return data;
    }

    /**
     * Extracted file reading method
     * I'd prefer to call this directly on each add in order to guarantee fresh data
     *
     * @param username
     * @return Data from corresponding username file (whatever that could be read at least)
     * @throws IOException when csv reader derps out
     */
    private static List<EntryBean> getUserDataFromFile(String username) throws IOException {
        List<EntryBean> data = new ArrayList<>();
        if (!Paths.get(getPath(username)).toFile().exists()) {
            return new ArrayList<>();
        }

        try (ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(getPath(username)), CsvPreference.STANDARD_PREFERENCE)) {
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
        }

        return data;
    }
}
