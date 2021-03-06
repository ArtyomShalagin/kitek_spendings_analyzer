package server;

import dataflow.DataManager;
import dataflow.EntryBean;
import fts_api.FTSApi;
import fts_api.data.FTSResult;
import fts_api.data.ReceiptInfo;
import py_interface.PyMlInterface;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerUtil {

    /**
     * Request receipt info from fts api, parse the response,
     * invoke ml magic to get categories, update databases
     *
     * @return fully parsed ReceiptInfo or null on error
     */
    public static ReceiptInfo handleNewReceipt(String username, String fn, String fd, String fpd) {
        FTSResult result;
        try {
            result = FTSApi.requestReceiptInfo(fn, fd, fpd);
        } catch (IOException e) {
            System.err.println("Unable to get data from FTS api: " + e.getMessage());
            return null;
        }
        if (result.getResponseCode() != 200) {
            System.err.println("FTS api returned code " + result.getResponseCode());
            return null;
        }
        ReceiptInfo receipt = ReceiptInfo.parseReceiptInfo(result.getData());
        if (receipt == null) {
            System.err.println("Unable to parse receipt from FTS api response");
            return null;
        }
        PyMlInterface mlInterface = new PyMlInterface();
        List<String> itemNames = receipt.items.stream()
                .map(entryInfo -> entryInfo.name)
                .collect(Collectors.toList());
        List<Integer> categories = mlInterface.getCategories(itemNames);

        if (categories == null) {
            return null;
        }
        for (int i = 0; i < categories.size(); i++) {
            receipt.items.get(i).category = categories.get(i);
        }

        List<EntryBean> entryBeans = DataManager.receiptToBeans(receipt);
        try {
            DataManager.appendUserData(username, entryBeans);
        } catch (IOException e) {
            System.err.println("Unable to update database: " + e.getMessage());
        }

        return receipt;
    }

    public static Map<String, String> parseParams(String data) {
        Map<String, String> result = new HashMap<>();
        Arrays.stream(data.split("&"))
                .forEach(token -> {
                    String[] parts = token.split("=");
                    result.put(parts[0], parts[1]);
                });
        return result;
    }

    public static String decodeOrNull(String data) throws UnsupportedEncodingException {
        return data == null ? null : URLDecoder.decode(data, "UTF-8");
    }

    public static List<String> parseList(String from) {
        if (from == null) {
            return null;
        }
        try {
            from = URLDecoder.decode(from, "UTF-8");
            String[] list = from.split("\\.");
            return Arrays.asList(list);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void uploadFile(Response response, String filename) throws IOException {
        Path path = Paths.get(filename);
        byte[] data = Files.readAllBytes(path);

        HttpServletResponse raw = response.raw();
        raw.getOutputStream().write(data);
        raw.getOutputStream().flush();
        raw.getOutputStream().close();
    }

    public static List<EntryBean> getFiltered(String username, Predicate<EntryBean> pred) throws IOException {
        return DataManager.getUserData(username).parallelStream().filter(pred).collect(Collectors.toList());
    }

    public static List<EntryBean> readAndFilterData(String username, String begin, String end) throws IOException {
        return getFiltered(username, Predicates.dateRange(begin, end));
    }


    public static class Predicates {
        private Predicates() {}

        public static Predicate<EntryBean> dateRange(String begin, String end) {
            Date beginDate = Date.valueOf(begin);
            Date endDate = Date.valueOf(end);

            return entryBean -> {
                Date entryDate = Date.valueOf(entryBean.date);
                return entryDate.compareTo(beginDate) >= 0 && entryDate.compareTo(endDate) <= 0;
            };
        }

        public static <T> Predicate<EntryBean> matchingList(Function<EntryBean, ? extends T> getter,
                                                            List<? super T> items) {

            return (it) -> items.contains(getter.apply(it));
        }
//
//        // hehe xd begins here
//        public static <T> Predicate<EntryBean> matchingAnyOf(Function<EntryBean, ? extends T> getter,
//                                                             List<Predicate<? super T>> filters,
//                                                             BinaryOperator<Predicate<? super T>> combiner) {
//            Predicate<? super T> total = filters.parallelStream().reduce(combiner).orElse((it) -> false);
//            return entryBean -> total.test(getter.apply(entryBean));
//        }
//
//        public static <T> Predicate<EntryBean> mapMatching(Function<EntryBean, ? extends T> getter, Predicate<? super T> filter) {
//            return entryBean -> filter.test(getter.apply(entryBean));
//        }
//
//        public static Predicate<EntryBean> matchingCategories(List<String> cats) {
//            return cats.parallelStream().map(Predicates::matchingCategory).reduce((it) -> false, Predicate::or);
//        }
//
//        public static Predicate<EntryBean> matchingCategory(String cat) {
//            return entryBean -> entryBean.category.equals(cat);
//        }
    }


}
