import com.fasterxml.jackson.databind.ObjectMapper;
import dataflow.DataManager;
import dataflow.EntryBean;
import entity.front.CatValue;
import fts_api.data.ReceiptInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py_interface.PyVisualizerInterface;
import server.ServerUtil;
import spark.Request;
import spark.Response;
import util.Pair;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
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
            get("/file", (req, res) -> {
                String name = req.queryParams("filename");
                if (name == null) {
                    res.status(400);
                    return "filename not specified";
                }

                if (!Paths.get(name).toFile().exists()) {
                    res.status(400);
                    return "file does not exist";
                }

                ServerUtil.uploadFile(res, name);
                return ""; // wont be empty due to upload
            });

            get("/stats", (req, res) -> {
                String type = req.queryParams("type");
                String username = req.queryParams("username");
                String filename;
                CatValue ret;
                List<Pair<String, Integer>> result;
                if (type == null) {
                    res.status(400);
                    return "type not specified";
                }
                if (username == null) {
                    res.status(400);
                    return "username not specified";
                }
                String beginPeriod = req.queryParams("begin_period");
                String endPeriod = req.queryParams("end_period");

                // this is ok while all stats queries require periods
                // but should we move it in every switch branch?
                if (beginPeriod == null || endPeriod == null) {
                    res.status(400);
                    return "begin or end period not specified";
                }

                switch (type) {
                    case "general_stats":
                        Predicate<EntryBean> pred = ServerUtil.Predicates.dateRange(beginPeriod, endPeriod);
                        String path = filterToPath(username, pred);
                        result = visualizerInterface.generalStats(path);
                        ret = new CatValue();
                        if (result != null) {
                            ret.result = result.parallelStream().map((p) -> new CatValue.CatValuePair(p.first, p.second)).collect(Collectors.toList());
                            return objectMapper.writeValueAsString(ret);
                        } else {
                            res.status(400);
                            return "no result obtained: what have i become";
                        }
                    case "max_spendings":
//                        data = ServerUtil.readAndFilterData(username, beginPeriod, endPeriod);
                        pred = ServerUtil.Predicates.dateRange(beginPeriod, endPeriod);
                        path = filterToPath(username, pred);
                        int itemCount = Integer.parseInt(req.queryParams("amount_items"));
                        result = visualizerInterface.maxSpendings(path, itemCount);
                        ret = new CatValue();
                        if (result != null) {
                            ret.result = result.parallelStream().map((p) -> new CatValue.CatValuePair(p.first, p.second)).collect(Collectors.toList());
                            return objectMapper.writeValueAsString(ret);
                        } else {
                            res.status(400);
                            return "no result obtained: what have i become";
                        }
                    case "weekly_spendings":
                    case "days_of_week_spending":
                        pred = ServerUtil.Predicates.dateRange(beginPeriod, endPeriod);
                        String daysRaw = req.queryParams("days_of_week");
                        List<String> days = ServerUtil.parseList(daysRaw);
                        String catsRaw = req.queryParams("categories");
                        List<String> cats = ServerUtil.parseList(catsRaw);

                        if (days != null) {
                            pred.and(ServerUtil.Predicates.matchingList(EntryBean::getDayOfWeek, days));
                        }

                        if (cats != null) {
                            pred.and(ServerUtil.Predicates.matchingList(EntryBean::getCategory, cats));
                        }

                        path = filterToPath(username, pred);

                        filename = visualizerInterface.weeklySpendings(path);
                        ServerUtil.uploadFile(res, filename);
                        return "";
                    case "categories_spending":
                        pred = ServerUtil.Predicates.dateRange(beginPeriod, endPeriod);

                        catsRaw = req.queryParams("categories");
                        cats = ServerUtil.parseList(catsRaw);

                        if (cats != null) {
                            pred.and(ServerUtil.Predicates.matchingList(EntryBean::getCategory, cats));
                        }

                        path = filterToPath(username, pred);

                        filename = visualizerInterface.categoriesSpendings(path);
                        ServerUtil.uploadFile(res, filename);
                        return "";

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

    private String filterToPath(String username, Predicate<EntryBean> pred) throws IOException {
        List<EntryBean> data;
        data = ServerUtil.getFiltered(username, pred);
        String path = "visualization/" + username + ".csv";
        DataManager.writeToFile(path, data);
        return path;
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