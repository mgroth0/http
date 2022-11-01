@file:JvmName("HttpJvmKt")

package matt.http

import matt.file.MFile
import matt.file.commons.USER_HOME
import matt.http.url.MURL
import matt.lang.ILLEGAL
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.lang.go
import matt.log.tab
import matt.prim.byte.efficientlyTransferTo
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import kotlin.concurrent.thread

object TheInternet {
  /*ugh, won't work with gradle daemon. There has to be a way to set properties that are reset for the gradle daemon*/
  val wasAvailableInThisRuntime by lazy {
	isAvailable()
  }
  fun isAvailable(): Boolean {
	return try {
	  val url = URL("http://www.google.com")
	  val conn: URLConnection = url.openConnection()
	  conn.connect()
	  conn.getInputStream().close()
	  true
	} catch (e: MalformedURLException) {
	  throw RuntimeException(e)
	} catch (e: IOException) {
	  false
	}
  }
}




fun MURL.printHTTP(
  op: HTTPRequest.()->Unit,
) = httpString { op() }.go {
  println(it)
}

fun MURL.httpString(
  op: HTTPRequest.()->Unit,
) = http { op() }.bufferedReader().readText()

fun MURL.http(
  op: HTTPRequest.()->Unit = {},
): InputStream {
  val req = HTTPRequest(this)
  req.op()
  return req.connect()
}

@DslMarker
annotation class HTTPDslMarker

class HTTPRequest internal constructor(private val url: MURL) {
  private val con = url.jURL.openConnection() as HttpURLConnection
  var method: HTTPType
	get() = con.requestMethod.let { m -> HTTPType.values().first { it.name == m } }
	set(value) {
	  con.requestMethod = value.name
	}

  fun headers(op: HTTPHeaders.()->Unit) {
	HTTPHeaders(con).apply(op)
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

  internal fun connect(): InputStream {
	if (verbose) {
	  println("sending ${method.name} to $url")
	  println("request properties:")
	  con.requestProperties.forEach {
		tab("${it.key}:${it.value}")
	  }
	}
	writer?.go {
	  it.write()
	}
	con.connect()
	if (verbose) {
	  println("Response = ${con.responseCode}")
	  println("Message = ${con.responseMessage}")
	}
	con.errorStream?.readAllBytes()?.let {

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

	return con.inputStream

  }

  abstract inner class Writer {
	internal abstract fun write()
  }

  inner class BasicWriter(private val bytes: ByteArray): Writer() {
	override fun write() {
	  con.doOutput = true
	  con.outputStream.write(bytes)
	}
  }

  inner class AsyncWriter(private val file: MFile): Writer() {
	override fun write() {
	  con.doOutput = true
	  thread {
		println("running async data transfer")
		file.channel().efficientlyTransferTo(con.outputStream)
		println("finished running async data transfer")
	  }
	}
  }
}

/*headers?*/
@HTTPDslMarker
class HTTPHeaders internal constructor(private val con: HttpURLConnection) {


  var contentType: String by propProvider("Content-Type")
  var accept: String by propProvider("Accept")
  var auth: String by propProvider("Authorization")

  private fun propProvider(key: String) = provider {
	varProp(
	  getter = {
		con.getRequestProperty(key)
	  },
	  setter = {
		con.setRequestProperty(key, it)
	  }
	)
  }

}


object NetRC {
  private val netrc = USER_HOME[".netrc"]
  private val lines = netrc.readText().lines().map { it.trim() }
  val login = lines.first { it.startsWith("login") }.substringAfter("login").trim()
  val password = lines.first { it.startsWith("password") }.substringAfter("password").trim()
}


