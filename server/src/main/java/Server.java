import com.fasterxml.jackson.databind.ObjectMapper;
import entity.forward.FTSResponseObject;
import entity.front.ReceiptData;
import entity.front.StatsResponse;
import fts_api.FTSApi;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static spark.Spark.*;

public class Server implements AutoCloseable {
    private ObjectMapper objectMapper = new ObjectMapper();
    private ExecutorService apiExecutor = Executors.newFixedThreadPool(4); // but is it 4

    public Server() {
        path("/api", () -> {
            get("/stats", (req, res) -> {
                // wonder what our stat strategy is going to be
                StatsResponse hole = new StatsResponse();
                hole.someStats = "meme stats are meme stats, god damn it";

                String statsWritten = objectMapper.writeValueAsString(hole);
                res.status(200);

                return statsWritten;
            });

            get("/add_qr_direct", (req, res) -> {
                String fiscalSign = req.queryParams("fp");
                String fss = req.queryParams("fn");
                String tickets = req.queryParams("i");
                String s = processQr(fss, tickets, fiscalSign);

                res.status(200);
                return s;
            });

            post("/add_qr", (req, res) -> {

                // this is such a horrible way to "parse"
//                String fiscalSign = req.body().substring(req.body().indexOf("fp"), req.body().lastIndexOf('&'));
//                processQr(fss, tickets, fiscalSign);

                Map<String, String>

                res.status(200);
                return "okay.";
            });

            get("/dummy", (req, res) -> FTSApi.requestReceiptInfo("8710000101053109", "16308", "3495662645").getData());
        });
    }

    private String processQr(String fss, String tickets, String fiscalSign) {
//        apiExecutor.submit(() -> {
        try {
            // parse the request
//                        AddQrRequest reqParsed = objectMapper.readValue(req.body(), AddQrRequest.class);

            // poll the api
//                        NValidateQrRequest validateRequest = new NValidateQrRequest();
            // inits
            FTSApi.FTSResult ftsResult = FTSApi.requestReceiptInfo(fss, tickets, fiscalSign);
            FTSResponseObject ftsResponse = objectMapper.readValue(ftsResult.getData(), FTSResponseObject.class);

            // send it
            // obtain smth
//                NValidateQrResponse validateResponse = objectMapper.readValue(ftsResult.getData(), NValidateQrResponse.class);

            // map it to a csv on disk, obtain its name
//                        String resultingCsv = "somename.csv";

            ReceiptData data = new ReceiptData();
            for (FTSResponseObject.Item i : ftsResponse.document.receipt.items) {
                ReceiptData.ReceiptItem newItem = new ReceiptData.ReceiptItem();

                newItem.cost_one = i.price;
                newItem.name = i.name;
                newItem.quantity = i.quantity;
                newItem.sum = i.sum;

                data.items.add(newItem);
            }

            return objectMapper.writeValueAsString(data);
            // start python
//                        Runtime.getRuntime().exec("add_raws.py $resultingCsv");
        } catch (IOException e) {
            System.err.println("Error while handing post request: " + e.getMessage());
        }
//        });

        return null; // lull
    }

    @Override
    public void close() {
        apiExecutor.shutdown();
    }
}