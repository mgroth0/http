@file:JvmName("ConnectionJvmKt")

package matt.http.connection

import matt.lang.function.Consume
import java.io.InputStream
import java.net.HttpURLConnection
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class JHTTPConnection internal constructor(private val jCon: HttpURLConnection): HTTPResponse {
  val inputStream: InputStream by jCon::inputStream
  override val statusCode get() = jCon.responseCode
  var timeout: Duration?
	get() = jCon.readTimeout.takeIf { it != 0 }?.let {
	  require(it > 0)
	  it.milliseconds
	}
	set(value) {
	  jCon.readTimeout = value?.inWholeMilliseconds?.toInt() ?: 0
	}
  override val text by lazy {
	inputStream.bufferedReader().readText()
  }

  override val statusMessage: String
	get() = jCon.responseMessage

  override fun toString(): String {
	return "JHTTPConnection[status=${statusCode},message=${statusMessage}]"
  }

}

val HTTPResponse.inputStream get() = (this as JHTTPConnection).inputStream


class JHTTPAsyncConnection(private val resultGetter: ()->HTTPConnectResult): HTTPAsyncConnection {

  override fun whenDone(op: Consume<HTTPConnectResult>) {
	/*obviously this can be done way more efficiently*/
	thread {
	  op(resultGetter())
	}
  }

}