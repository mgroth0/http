@file:JvmName("ConnectionJvmKt")

package matt.http.connection

import matt.lang.function.Consume
import java.io.InputStream
import java.net.HttpURLConnection
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class JHTTPConnection internal constructor(private val jCon: HttpURLConnection): HTTPResponse {


  fun checkForErrorMessages() {


	jCon.errorStream?.readAllBytes()?.let {

	  /*
	  *
	  *   * <p>This method will not cause a connection to be initiated.  If
	  * the connection was not connected, or if the server did not have
	  * an error while connecting or if the server had an error but
	  * no error data was sent, this method will return null. This is
	  * the default.
	  *
	  * @return an error stream if any, null if there have been no
	  * errors, the connection is not connected or the server sent no
	  * useful data.
	  * */

	  println("ERR: ${it.decodeToString()}")
	} ?: run {
	  /*if (verbose) {*/
	  println("no error message from server")
	  /*}*/
	}

  }

  val inputStream: InputStream
	get() {
	  try {
		return jCon.inputStream
	  } catch (e: Exception) {
		checkForErrorMessages()
		throw e
	  }
	}


  override val statusCode: Int
	get() {
	  try {
		return jCon.responseCode
	  } catch (e: Exception) {
		checkForErrorMessages()
		throw e
	  }
	}


  var timeout: Duration?
	get() = jCon.readTimeout.takeIf { it != 0 }?.let {
	  require(it > 0)
	  it.milliseconds
	}
	set(value) {
	  jCon.readTimeout = value?.inWholeMilliseconds?.toInt() ?: 0
	}
  override val bytes: ByteArray
	get() = inputStream.readAllBytes()

  override val text by lazy {
	inputStream.bufferedReader().readText()
  }

  override val statusMessage: String
	get() {
	  try {
		return jCon.responseMessage
	  } catch (e: Exception) {
		checkForErrorMessages()
		throw e
	  }

	}

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