package matt.http.connection

import java.io.InputStream
import java.net.HttpURLConnection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds



class JHTTPConnection internal constructor(private val jCon: HttpURLConnection): HTTPConnection {
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

  override fun print() {
	println(text)
  }

  override fun getRequestProperty(name: String): String? {
	return jCon.getRequestProperty(name)
  }

  override fun setRequestProperty(name: String, value: String?) {
	jCon.setRequestProperty(name, value)
  }
}