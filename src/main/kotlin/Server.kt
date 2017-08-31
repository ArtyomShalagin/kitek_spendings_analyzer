import com.fasterxml.jackson.databind.ObjectMapper
import entity.forward.NValidateQrRequest
import entity.forward.NValidateQrResponse
import entity.front.AddQrRequest
import entity.front.StatsResponse
import spark.Request
import spark.Spark.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Server : AutoCloseable {
    private val objectMapper: ObjectMapper = ObjectMapper()
    private val apiExecutor: ExecutorService = Executors.newFixedThreadPool(4) // but is it 4

    init {
        path("/api") {
            get("/stats") { req, res ->
                // wonder what our stat strategy is going to be hehe xd
                val hole = StatsResponse()
                hole.someStats = "meme stats are meme stats, god damn it"

                val statsWritten = objectMapper.writeValueAsString(hole)
                res.status(200)

                statsWritten
            }

            post("/add_qr") { req, res ->
                apiExecutor.submit {
                    // parse the request
                    val reqParsed = objectMapper.readValue(req.body(), AddQrRequest::class.java)

                    // poll the api
                    val validateRequest = NValidateQrRequest()
                    // inits
                    val bytesSend: ByteArray = objectMapper.writeValueAsBytes(validateRequest)

                    // send it
                    // obtain smth
                    val bytesRecv: ByteArray = "{}".toByteArray()
                    val validateResponse = objectMapper.readValue(bytesRecv, NValidateQrResponse::class.java)

                    // map it to a csv on disk, obtain its name
                    val resultingCsv = "somename.csv"

                    // start the damn python
                    Runtime.getRuntime().exec("add_raws.py $resultingCsv")

                }

                res.status(200)

                "meme add"
            }
        }
    }

    override fun close() {
        apiExecutor.shutdown()
    }

}