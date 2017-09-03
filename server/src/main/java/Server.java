import com.fasterxml.jackson.databind.ObjectMapper;
import entity.front.CatValue;
import fts_api.data.ReceiptInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py_interface.PyVisualizerInterface;
import server.ServerUtil;
import spark.Request;
import spark.Response;
import util.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Server implements AutoCloseable {
    private static final String DATA_DIR = "user_data/";
    private ObjectMapper objectMapper = new ObjectMapper();
    private ExecutorService apiExecutor = Executors.newFixedThreadPool(4); // but is it 4
    private Logger logger = LoggerFactory.getLogger(Server.class);
    private PyVisualizerInterface visualizerInterface = new PyVisualizerInterface();

    public Server() {
        port(4567);

        path("/api", () -> {

            // note
            // this is such a fucking copypasta
            // i hope it at least works
            get("/stats", (req, res) -> {
                String type = req.queryParams("type");
                String name = null;
                String filename = null;
                CatValue ret = null;
                List<Pair<String, Integer>> result = null;
                if (type == null) {
                    res.status(400);
                    return "type not specified";
                }

                switch (type) {
                    case "general_stats":
//                        String beginPeriod = req.queryParams("begin_period");
//                        String endPeriod = req.queryParams("begin_period");
                        name = req.queryParams("username");
                        if (name == null) {
                            res.status(400);
                            return "missing params";
                        }

                        result = visualizerInterface.generalStats(getPath(name), 5); // five? fifty five? fuck all would i know
                        ret = new CatValue();
                        if (result != null) {
                            ret.result = result.parallelStream().map((p) -> new CatValue.CatValuePair(p.first, p.second)).collect(Collectors.toList());
                            return objectMapper.writeValueAsString(ret);
                        } else {
                            res.status(400);
                            return "no result obtained: what have i become";
                        }
                    case "max_spendings":
                        name = req.queryParams("username");
                        if (name == null) {
                            res.status(400);
                            return "missing params";
                        }

                        result = visualizerInterface.maxSpendings(getPath(name), 5);
                        ret = new CatValue();
                        if (result != null) {
                            ret.result = result.parallelStream().map((p) -> new CatValue.CatValuePair(p.first, p.second)).collect(Collectors.toList());
                            return objectMapper.writeValueAsString(ret);
                        } else {
                            res.status(400);
                            return "no result obtained: what have i become";
                        }
                    case "weekly_spendings":
                        name = req.queryParams("username");
                        if (name == null) {
                            res.status(400);
                            return "missing params";
                        }

                        filename = visualizerInterface.weeklySpendings(getPath(name));
                        return "{\"filename\":\"" + filename + "\"}";
                    case "categories_spendings":
                        name = req.queryParams("username");
                        if (name == null) {
                            res.status(400);
                            return "missing params";
                        }

                        filename = visualizerInterface.categoriesSpendings(getPath(name));
                        return "{\"filename\":\"" + filename + "\"}";

                    default:
                        res.status(400);
                        return "unknown type";
                }
            });

            post("/add_data", (req, res) -> {
                Map<String, String> params = ServerUtil.parseParams(req.body());
                String type = params.get("type");

                switch (type) {
                    case "receipt":
                        String fn = params.get("fn");
                        String fd = params.get("fd");
                        String fpd = params.get("fpd");
                        if (fn == null || fd == null || fpd == null) {
                            System.err.println("Post request has invalid data params");
                            res.status(400);
                            return "err";
                        }
                        String username = params.get("username");
                        if (username == null) {
                            System.err.println("Post request does not have username param");
                            res.status(400);
                            return "err";
                        }
                        ReceiptInfo receipt = ServerUtil.handleNewReceipt(username, fn, fd, fpd);
                        res.status(200);
                        return objectMapper.writeValueAsString(receipt);
                    default:
                        res.status(400);
                        return "unknown tp";
                }
            });
            path("/proposed", () -> {
                path("/add_data", () -> {
                    post("/receipt", (req, res) -> {
                        Map<String, String> params = ServerUtil.parseParams(req.body());

                        String fn = params.get("fn");
                        String fd = params.get("fd");
                        String fpd = params.get("fpd");
                        if (fn == null || fd == null || fpd == null) {
                            System.err.println("Post request has invalid data params");
                            res.status(400);
                            return "err";
                        }
                        String username = params.get("username");
                        if (username == null) {
                            System.err.println("Post request does not have username param");
                            res.status(400);
                            return "err";
                        }
                        ReceiptInfo receipt = ServerUtil.handleNewReceipt(username, fn, fd, fpd);
                        res.status(200);
                        return objectMapper.writeValueAsString(receipt);
                    });

                    path("/direct", () -> {
                        get("/receipt", (req, res) -> {
                            ReceiptInfo receipt = ServerUtil.handleNewReceipt(
                                    req.queryParams("username"),
                                    req.queryParams("fn"),
                                    req.queryParams("fd"),
                                    req.queryParams("fpd"));
                            return objectMapper.writeValueAsString(receipt);
                        });
                    });
                });
            });
        });

        notFound(this::fourOhFourClause);
        internalServerError(this::fiveOhOhClause);
        exception(Exception.class, this::exceptionClause);

    }

    private String fourOhFourClause(Request req, Response res) {
        logger.info(" -- 404 -- ");
        logRequestInfo(req);
        logger.info("   ");

        res.status(404);
        return "unknown op: " + req.params("any");
    }


    private String fiveOhOhClause(Request req, Response res) {
        logger.info(" -- 500 -- ");
        logRequestInfo(req);
        logger.info("   ");

        res.status(500);
        return "error on op: " + req.params("any");
    }

    private String exceptionClause(Exception e, Request req, Response res) {
        logger.info(" -- Exception -- ");
        logRequestInfo(req);
        logger.info("   ");
        logger.trace("With trace: ", e);
        logger.trace("   ");


        res.status(505);
        return "error on op: " + req.params("any");
    }

    private void logRequestInfo(Request req) {
        logger.info("Request @ " + req.pathInfo());
        logger.info("With query:" + req.queryString());
        logger.info("With body:" + req.body());
    }

    /**
     * god fucking damn it we should really sort out the directory model.
     * copypasta from datamanager
     *
     * @param username
     * @return
     */
    @Deprecated
    private static String getPath(String username) {
        return DATA_DIR + username + ".csv";
    }

    @Override
    public void close() {
        apiExecutor.shutdown();
    }
}