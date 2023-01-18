package matt.http.req

import matt.file.MFile
import matt.http.connection.ConnectionRefused
import matt.http.connection.HTTPConnectResult
import matt.http.connection.JHTTPConnection
import matt.http.connection.Timeout
import matt.http.headers.HTTPHeaders
import matt.http.method.HTTPMethod
import matt.http.url.MURL
import matt.lang.ILLEGAL
import matt.lang.go
import matt.log.tab
import matt.log.warn.warn
import matt.prim.byte.efficientlyTransferTo
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class JHTTPRequest internal constructor(private val url: MURL): HTTPRequest {
  init {
	println("opening connection to ${url.jURL}")
	warn("it is weird and does not match javascript that ths happens automatically")
  }

  private val jCon = url.jURL.openConnection() as HttpURLConnection
  private val con = JHTTPConnection(jCon)


  override fun getRequestProperty(name: String): String? {
	return jCon.getRequestProperty(name)
  }

  override fun setRequestProperty(name: String, value: String?) {
	jCon.setRequestProperty(name, value)
  }

  override var method: HTTPMethod
	get() = jCon.requestMethod.let { m -> HTTPMethod.values().first { it.name == m } }
	set(value) {
	  jCon.requestMethod = value.name
	}
  override var timeout: Duration?
	get() = jCon.connectTimeout.takeIf { it != 0 }?.let {
	  require(it > 0)
	  it.milliseconds
	}
	set(value) {
	  jCon.connectTimeout = value?.inWholeMilliseconds?.toInt() ?: 0
	}

  fun headers(op: HTTPHeaders.()->Unit) {
	HTTPHeaders(this).apply(op)
  }

  var verbose = false

  var writer: Writer? = null
	set(value) {
	  require(field == null)
	  field = value
	}

  var data: ByteArray
	get() = ILLEGAL
	set(value) {
	  writer = BasicWriter(value)
	}

  fun writeAsync(file: MFile) {
	writer = AsyncWriter(file)
  }

  internal fun connect(): HTTPConnectResult {
	if (verbose) {
	  println("sending ${method.name} to $url")
	  println("request properties:")
	  jCon.requestProperties.forEach {
		tab("${it.key}:${it.value}")
	  }
	}
	writer?.go {
	  it.write()
	}
	try {
	  jCon.connect()
	} catch (e: SocketTimeoutException) {
	  println("Timeout!")
	  return Timeout
	} catch (e: ConnectException) {
	  if (e.message?.trim() == "Connection refused") return ConnectionRefused
	  else throw e
	}

	if (verbose) {
	  println("Response = ${jCon.responseCode}")
	  println("Message = ${jCon.responseMessage}")
	}
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
	  if (verbose) {
		println("no error message from server")
	  }
	}

	return con

  }

  abstract inner class Writer {
	internal abstract fun write()
  }

  inner class BasicWriter(private val bytes: ByteArray): Writer() {
	override fun write() {
	  jCon.doOutput = true
	  jCon.outputStream.write(bytes)
	}
  }

  inner class AsyncWriter(private val file: MFile): Writer() {
	override fun write() {
	  jCon.doOutput = true
	  thread {
		println("running async data transfer")
		file.readChannel().efficientlyTransferTo(jCon.outputStream)
		println("finished running async data transfer")
	  }
	}
  }
}

