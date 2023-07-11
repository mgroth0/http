package matt.http.connection

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import matt.http.report.HTTPResponseReport
import matt.prim.str.mybuild.lineDelimitedString


sealed interface HTTPConnectResult

suspend fun HTTPConnectResult.requireSuccessful() = (this as HTTPConnection).requireSuccessful()

abstract class HTTPConnectionProblem(
    uri: String,
    message: String,
    cause: Throwable? = null
) : IOException("$uri: $message", cause),
    HTTPConnectResult

abstract class HTTPConnectionProblemWithResponse(
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

}, cause = cause), HTTPConnectResult

class HTTPConnection(private val response: HttpResponse) : HTTPConnectResult {


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


    suspend fun consumeLines(op: (String) -> Unit) {
        val channel = response.bodyAsChannel()
        do {
            val nextLine = channel.readUTF8Line()
            if (nextLine != null) op(nextLine)
        } while (nextLine != null)
    }

}


