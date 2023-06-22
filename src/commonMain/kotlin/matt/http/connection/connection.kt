package matt.http.connection

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


sealed interface HTTPConnectResult

suspend fun HTTPConnectResult.requireSuccessful() = (this as HTTPConnection).requireSuccessful()

abstract class HTTPConnectionProblem(
    uri: String,
    message: String,
    cause: Throwable? = null
) : IOException("$uri: $message", cause),
    HTTPConnectResult

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


