package matt.http.lib

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.utils.EmptyContent
import io.ktor.http.HttpMethod
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.util.InternalAPI
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPConnection
import matt.http.method.HTTPMethod
import matt.http.req.requester.problems.HTTPExceptionWhileCreatingConnection
import matt.http.req.write.BodyWriter
import matt.http.req.write.BytesBodyWriter
import matt.http.req.write.DuringConnectionWriter
import matt.http.req.write.NoBody
import matt.lang.go
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


  fun initialize(url: String, method: HTTPMethod, bodyWriter: BodyWriter) {
	builder.url(url)
	builder.method
	builder.method = HttpMethod(method.name)
	builder.body = figureOutContentWriter(bodyWriter)
  }

  fun applyHeaders(headers: List<Pair<String, String>>) {
	headers.forEach { (k, v) ->
	  builder.headers {
		this.append(k, v)
	  }
	}
  }


  fun applyTimeout(timeout: Duration?) {
	timeout?.inWholeMilliseconds?.go {
	  builder.timeout {
		connectTimeoutMillis = it
		requestTimeoutMillis = it
		socketTimeoutMillis = it
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