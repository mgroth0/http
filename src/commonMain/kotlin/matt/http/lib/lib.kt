package matt.http.lib

import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import matt.http.connection.HTTPConnection
import matt.http.connection.SingleHTTPConnectResult
import matt.http.method.HTTPMethod
import matt.http.req.requester.problems.HTTPExceptionWhileCreatingConnection
import matt.http.req.write.BodyWriter
import matt.http.req.write.BytesBodyWriter
import matt.http.req.write.DuringConnectionWriter
import matt.http.req.write.NoBody
import matt.lang.anno.SeeURL
import kotlin.time.Duration

val REQUIRES_COOKIES = listOf(
    "twitter.com"
)


expect val httpClientEngine: HttpClientEngine

interface MyHttpRequestBuilderInter {
    val builder: HttpRequestBuilder

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

    fun applyTimeout(timeout: Duration) {
        if (timeout != Duration.INFINITE) {
            val to = timeout.inWholeMilliseconds
            builder.timeout {
                connectTimeoutMillis = to
                requestTimeoutMillis = to
                socketTimeoutMillis = to
            }
        }
    }
}

class MyPrebuiltHttpRequestBuilder(override val builder: HttpRequestBuilder) : MyHttpRequestBuilderInter


@OptIn(InternalAPI::class)
class MyNewHTTPRequestBuilder : MyHttpRequestBuilderInter {


    companion object {
        private val client = io.ktor.client.HttpClient(httpClientEngine) {
            install(HttpTimeout)
        }
    }

    override val builder = HttpRequestBuilder()


    fun initialize(
        url: String,
        method: HTTPMethod,
        bodyWriter: BodyWriter
    ) {
        if (REQUIRES_COOKIES.any { it in url }) {
            @SeeURL("https://webmasters.stackexchange.com/a/74509")
            @SeeURL("https://github.com/ktorio/ktor/blob/e632bf5a943d44e1fb009bca8c5aa35d19bd6059/ktor-client/ktor-client-core/common/src/io/ktor/client/plugins/HttpSend.kt#L132")
            throw (RequiresCookiesException(url))
        }
        builder.url(url)
        builder.method = HttpMethod(method.name)
        builder.body = figureOutContentWriter(bodyWriter)
    }


    suspend fun send(): SingleHTTPConnectResult {
        return try {
            val con = client.request(builder)
            HTTPConnection(builder.attributes, con)
        } catch (e: Exception) {
            HTTPExceptionWhileCreatingConnection(
                uri = builder.url.toString(),
                cause = e,
                requestAttributes = builder.attributes
            )
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

@SeeURL("https://webmasters.stackexchange.com/a/74509")
@SeeURL("https://github.com/ktorio/ktor/blob/e632bf5a943d44e1fb009bca8c5aa35d19bd6059/ktor-client/ktor-client-core/common/src/io/ktor/client/plugins/HttpSend.kt#L132")
class RequiresCookiesException(url: String) :
    Exception("I think $url keeps redirecting until it sees that you \"set cookies\"")