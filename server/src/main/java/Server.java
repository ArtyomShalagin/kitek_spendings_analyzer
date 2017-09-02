import com.fasterxml.jackson.databind.ObjectMapper;
import entity.forward.NValidateQrRequest;
import entity.forward.NValidateQrResponse;
import entity.front.AddQrRequest;
import entity.front.StatsResponse;

import java.io.IOException;
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


                return "ok";
            });

            post("/add_qr", (req, res) -> {
                apiExecutor.submit(() -> {
                    try {
                        // parse the request
                        AddQrRequest reqParsed = objectMapper.readValue(req.body(), AddQrRequest.class);

                        // poll the api
                        NValidateQrRequest validateRequest = new NValidateQrRequest();
                        // inits
                        byte[] bytesSend = objectMapper.writeValueAsBytes(validateRequest);

                        // send it
                        // obtain smth
                        byte[] bytesRecv = "{}".getBytes();
                        NValidateQrResponse validateResponse = objectMapper.readValue(bytesRecv, NValidateQrResponse.class);

                        // map it to a csv on disk, obtain its name
                        String resultingCsv = "somename.csv";

                        // start python
                        Runtime.getRuntime().exec("add_raws.py $resultingCsv");
                    } catch (IOException e) {
                        System.err.println("Error while handing post request: " + e.getMessage());
                    }
                });

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