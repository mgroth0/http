package matt.http.connection

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import matt.http.report.HTTPResponseReport
import matt.prim.str.mybuild.api.lineDelimitedString

sealed interface HTTPConnectResult

sealed interface MultipleHTTPConnectResult : HTTPConnectResult {
    val requestAttributes: List<Attributes>
}

sealed interface SingleHTTPConnectResult : HTTPConnectResult {
    val requestAttributes: Attributes
}

suspend fun HTTPConnectResult.requireSuccessful() = (this as HTTPConnection).requireSuccessful()

/*class ImmutableRequestData(
    val request: ImmutableHTTPRequest,
    val attributes: Attributes
)*/


sealed class HTTPConnectionProblem(
    uri: String,
    message: String,
    cause: Throwable? = null
) : IOException("$uri: $message", cause),
    HTTPConnectResult



abstract class HTTPConnectionProblemWithMultipleRequests(
    uri: String,
    message: String,
    override val requestAttributes: List<Attributes>,
    cause: Throwable? = null,
) : IOException("$uri: $message", cause),
    HTTPConnectResult, MultipleHTTPConnectResult

abstract class HTTPConnectionProblemNoResponse(
    uri: String,
    message: String,
    override val requestAttributes: Attributes,
    cause: Throwable? = null
) : HTTPConnectionProblem(uri=uri,message=message,cause=cause),
    SingleHTTPConnectResult

abstract class HTTPConnectionProblemWithResponse(
    override val requestAttributes: Attributes,
    uri: String,
    message: String,
    cause: Throwable? = null,
    status: HttpStatusCode,
    headers: Headers,
    responseBody: String
) : HTTPConnectionProblem(uri = uri, message = lineDelimitedString {
    +"message: $message"
    +HTTPResponseReport(
        status = status,
        headers = headers.entries().toList(),
        body = responseBody
    ).text

}, cause = cause), SingleHTTPConnectResult

class HTTPConnection(
    override val requestAttributes: Attributes,
    private val response: HttpResponse
) : SingleHTTPConnectResult {


    private var alreadyReadBytes: ByteArray? = null
    private val mutex = Mutex()

    suspend fun bytes(): ByteArray {
        mutex.withLock {
            if (alreadyReadBytes == null) {
                alreadyReadBytes = response.readBytes()
            }
            return alreadyReadBytes!!
        }
    }

    suspend fun text(): String {
        return bytes().decodeToString()
    }

    private var alreadyGotHTTPStatusCode: HttpStatusCode? = null
    private val statusMutex = Mutex()

    suspend fun statusCode(): HttpStatusCode {
        statusMutex.withLock {
            if (alreadyGotHTTPStatusCode == null) {
                alreadyGotHTTPStatusCode = response.status
            }
            return alreadyGotHTTPStatusCode!!
        }
    }

    private var alreadyGotHTTPHeaders: Headers? = null
    private val headersMutex = Mutex()

    suspend fun headers(): Headers {
        statusMutex.withLock {
            if (alreadyGotHTTPHeaders == null) {
                alreadyGotHTTPHeaders = response.headers
            }
            return alreadyGotHTTPHeaders!!
        }
    }


    suspend fun contentType() = headers()[HttpHeaders.ContentType]?.let { ContentType.parse(it) }

    suspend fun requireSuccessful(): HTTPConnection {
        bytes()
        return this
    }

    suspend fun print() {
        println(statusCode())
        println(text())
    }

    suspend fun bodyAsChannel() = response.bodyAsChannel()

    suspend fun consumeLines(op: (String) -> Unit) {
        val channel = response.bodyAsChannel()
        do {
            val nextLine = channel.readUTF8Line()
            if (nextLine != null) op(nextLine)
        } while (nextLine != null)
    }

}


