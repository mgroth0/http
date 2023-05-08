package matt.http.lib

import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPConnection
import matt.http.method.HTTPMethod
import matt.http.req.requester.problems.HTTPExceptionWhileCreatingConnection
import matt.http.req.write.BodyWriter
import matt.http.req.write.BytesBodyWriter
import matt.http.req.write.DuringConnectionWriter
import matt.http.req.write.NoBody
import kotlin.time.Duration

expect val httpClientEngine: HttpClientEngine

@OptIn(InternalAPI::class)
class HTTPRequestBuilder {

    companion object {
        private val client = io.ktor.client.HttpClient(httpClientEngine) {

            install(HttpTimeout)

            /*	  this.engine {
                    this.lo
                  }
                  this.l
                  this.logging {

                  }
                  install(Logging) {
                    level = LogLevel.NONE
                    logger = Logger.EMPTY
                    this
                  }*/
        }
    }

    var builder = HttpRequestBuilder()


    fun initialize(
        url: String,
        method: HTTPMethod,
        bodyWriter: BodyWriter
    ) {


        builder.url(url)
        builder.method = HttpMethod(method.name)
        builder.body = figureOutContentWriter(bodyWriter)
    }

    fun applyHeaders(headers: List<Pair<String, String>>) {
        headers.forEach { (k, v) ->
            builder.headers {
                this.append(
                    k,
                    v
                )
            }
        }
    }


    fun applyTimeout(timeout: Duration?) {
        val to = timeout?.inWholeMilliseconds
        if (to != null) {
            builder.timeout {
                connectTimeoutMillis = to
                requestTimeoutMillis = to
                socketTimeoutMillis = to
            }
        }

    }

    suspend fun send(): HTTPConnectResult {
        return try {
            val con = client.request(builder)
            HTTPConnection(con)
        } catch (e: Exception) {
            HTTPExceptionWhileCreatingConnection(e)
        }
    }
}

fun figureOutContentWriter(bodyWriter: BodyWriter): OutgoingContent {
    return when (bodyWriter) {
        NoBody                    -> EmptyContent
        is BytesBodyWriter        -> ByteArrayContent(bodyWriter.bytes)
        is DuringConnectionWriter -> figureOutLiveContentWriter(bodyWriter)
    }
}

expect fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent