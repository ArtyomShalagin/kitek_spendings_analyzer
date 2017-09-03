import com.fasterxml.jackson.databind.ObjectMapper;
import entity.front.AddReceiptRequest;
import entity.front.StatsResponse;
import fts_api.data.ReceiptInfo;

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

            post("/add_data", (req, res) -> {
                res.status(200);
                try {
                    Map<String, String> params = ServerUtil.parseParams(req.body());
                    String type = params.get("type");
                    if (type == null) {
                        System.err.println("Post request has no type param");
                        return "err";
                    }
                    if (type.equals("receipt")) {
                        String fn = params.get("fn");
                        String fd = params.get("fd");
                        String fpd = params.get("fpd");
                        if (fn == null || fd == null || fpd == null) {
                            System.err.println("Post request has invalid data params");
                            return "err";
                        }
                        String username = params.get("username");
                        if (username == null) {
                            System.err.println("Post request does not have username param");
                            return "err";
                        }
                        ReceiptInfo receipt = ServerUtil.handleNewReceipt(username, fn, fd, fpd);
                        System.out.println(receipt);
                        return receipt;
                    } else {
                        System.err.println("Unknown add_data request type: " + type);
                    }

                    // parse the request
                    AddReceiptRequest reqParsed = objectMapper.readValue(req.body(), AddReceiptRequest.class);
//
//                        // poll the api
//                        NValidateQrRequest validateRequest = new NValidateQrRequest();
//                        // inits
//                        byte[] bytesSend = objectMapper.writeValueAsBytes(validateRequest);
//
//                        // send it
//                        // obtain smth
//                        byte[] bytesRecv = "{}".getBytes();
//                        NValidateQrResponse validateResponse = objectMapper.readValue(bytesRecv, NValidateQrResponse.class);
                } catch (IOException e) {
                    System.err.println("Error while handing post request: " + e.getMessage());
                }

                res.status(200);

                return "meme add";
            });
        });
    }

    @Override
    public void close() {
        apiExecutor.shutdown();
    }
}