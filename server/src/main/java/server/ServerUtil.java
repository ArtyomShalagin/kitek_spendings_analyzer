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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static void uploadFile(Response response, String filename) throws IOException {
        Path path = Paths.get(filename);
        byte[] data = Files.readAllBytes(path);

        HttpServletResponse raw = response.raw();
//        response.header("Content-Disposition", "attachment; filename=image.png");
//        response.type("application/force-download");
        raw.getOutputStream().write(data);
        raw.getOutputStream().flush();
        raw.getOutputStream().close();
    }
}
