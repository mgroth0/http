package matt.http.connection

import java.io.InputStream
import java.net.HttpURLConnection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed interface HTTPConnectResult {
  fun requireSuccessful() = this as JHTTPConnection
}

sealed interface HTTPConnectFailure: HTTPConnectResult

object Timeout: HTTPConnectFailure
object ConnectionRefused: HTTPConnectFailure

class JHTTPConnection internal constructor(private val jCon: HttpURLConnection): HTTPConnectResult, HTTPConnection {
  val inputStream: InputStream by jCon::inputStream
  val statusCode get() = jCon.responseCode
  var timeout: Duration?
	get() = jCon.readTimeout.takeIf { it != 0 }?.let {
	  require(it > 0)
	  it.milliseconds
	}
	set(value) {
	  jCon.readTimeout = value?.inWholeMilliseconds?.toInt() ?: 0
	}
  val text by lazy {
	inputStream.bufferedReader().readText()
  }

  fun print() {
	println(text)
  }

  override fun getRequestProperty(name: String): String? {
	return jCon.getRequestProperty(name)
  }

  override fun setRequestProperty(name: String, value: String?) {
	jCon.setRequestProperty(name, value)
  }
}