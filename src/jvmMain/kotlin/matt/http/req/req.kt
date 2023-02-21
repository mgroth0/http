package matt.http.req

import matt.file.FileOrURL
import matt.file.MFile
import matt.http.connection.ConnectionRefused
import matt.http.connection.HTTPAsyncConnection
import matt.http.connection.HTTPConnectResult
import matt.http.connection.JHTTPAsyncConnection
import matt.http.connection.JHTTPConnection
import matt.http.connection.Timeout
import matt.http.live.JLiveHTTPConnection
import matt.http.method.HTTPMethod
import matt.http.req.write.AsyncWriter
import matt.http.req.write.BasicHTTPWriter
import matt.lang.sync
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class HTTPRequestImpl internal actual constructor(override val url: FileOrURL): HTTPRequest() {

  //  init {
  //	println("configuring connection for ${url.cpath}")
  //	Thread.dumpStack()
  //  }

  private val jCon = URI(url.cpath).toURL().openConnection() as HttpURLConnection
  private val con = JHTTPConnection(jCon)


  override fun getRequestProperty(name: String): String? {
	return jCon.getRequestProperty(name)
  }

  actual override fun setRequestProperty(name: String, value: String?) {
	/*println("setting request property $name to $value")*/
	jCon.setRequestProperty(name, value)
  }

  actual override fun allRequestHeaders() = jCon.requestProperties


  actual override var method: HTTPMethod
	get() = jCon.requestMethod.let { m -> HTTPMethod.values().first { it.name == m } }
	set(value) {
	  jCon.requestMethod = value.name
	}
  actual var timeout: Duration?
	get() = jCon.connectTimeout.takeIf { it != 0 }?.let {
	  require(it > 0)
	  it.milliseconds
	}
	set(value) {
	  jCon.connectTimeout = value?.inWholeMilliseconds?.toInt() ?: 0
	}


  fun writeAsync(file: MFile) {
	outputDuringConnection {
	  AsyncWriter(this, file).write()
	}
  }


  override fun configureForWritingBytes(bytes: ByteArray) {
	outputDuringConnection {
	  BasicHTTPWriter(this, bytes).write()
	}
  }


  private var didLiveConnectionOps = false
  private val liveConnectionOps = mutableListOf<JLiveHTTPConnection.()->Unit>()

  @Synchronized
  fun outputDuringConnection(op: JLiveHTTPConnection.()->Unit) {
	require(!didLiveConnectionOps)
	jCon.doOutput = true
	liveConnectionOps += op
  }


  actual override fun openConnection(): HTTPConnectResult {
	println("openConnection1")
	return try {
	  println("openConnection2")
	  jCon.connect()

	  println("openConnection3")


	  if (liveConnectionOps.isNotEmpty()) {
		sync {
		  val liveConnection = JLiveHTTPConnection(jCon)
		  liveConnectionOps.forEach {
			liveConnection.it()
		  }
		  liveConnection.outputStream.close()
		  didLiveConnectionOps = true
		}
	  }




	  println("openConnection4")
	  jCon.errorStream?.readAllBytes()?.let {
		println("openConnection4.5")

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
		if (verbose) {
		  println("no error message from server")
		}
	  }
	  println("openConnection5")
	  con
	} catch (e: SocketTimeoutException) {
	  println("Timeout!")
	  return Timeout
	} catch (e: ConnectException) {
	  if (e.message?.trim() == "Connection refused") return ConnectionRefused
	  else throw e
	}
  }

  override fun openAsyncConnection(): HTTPAsyncConnection {
	var r: HTTPConnectResult? = null
	val t = thread {
	  r = openConnection()
	}
	return JHTTPAsyncConnection {
	  t.join()
	  r!!
	}
  }

}


