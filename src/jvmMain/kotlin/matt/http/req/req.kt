package matt.http.req

import matt.file.FileOrURL
import matt.file.MFile
import matt.http.connection.ConnectionRefused
import matt.http.connection.HTTPAsyncConnection
import matt.http.connection.HTTPConnectResult
import matt.http.connection.JHTTPConnection
import matt.http.connection.Timeout
import matt.http.connection.async.JHTTPAsyncConnection
import matt.http.live.JLiveHTTPConnection
import matt.http.method.HTTPMethod
import matt.http.method.HTTPMethod.DELETE
import matt.http.method.HTTPMethod.GET
import matt.http.method.HTTPMethod.PATCH
import matt.http.method.HTTPMethod.POST
import matt.http.method.HTTPMethod.PUT
import matt.http.req.write.AsyncWriter
import matt.http.req.write.BasicHTTPWriter
import matt.lang.anno.SeeURL
import matt.lang.sync
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpRequest.Builder
import java.net.http.HttpResponse.BodyHandlers
import kotlin.concurrent.thread
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration


actual class HTTPRequestImpl internal actual constructor(override val url: FileOrURL): HTTPRequest() {

  @SeeURL("https://www.baeldung.com/java-9-http-client")
  private var jCon: HttpRequest = HttpRequest.newBuilder().uri(URI(url.cpath)).GET().build()

  private val writeStreams by lazy {
	val output = PipedOutputStream()
	val input = PipedInputStream(output)
	input to output
  }
  private val outputStream by lazy { writeStreams.second }

  private fun cloner(): Builder = HttpRequest.newBuilder(jCon) { _, _ -> true }
  private fun publisher(): BodyPublisher = BodyPublishers.ofInputStream { writeStreams.first }


  @Synchronized override fun getRequestProperty(name: String): String? {
	return allRequestHeaders()[name]?.single()
  }

  @Synchronized actual override fun setRequestProperty(name: String, value: String?) {
	require(!didConnect)
	jCon = if (value == null) {
	  HttpRequest.newBuilder(jCon) { k, _ -> k != name }.build()
	} else {
	  cloner().header(name, value).build()
	}
  }

  @Synchronized
  actual override fun allRequestHeaders(): Map<String, List<String>> = jCon.headers().map().toMap()


  actual override var method: HTTPMethod
	@Synchronized get() = jCon.method().let { m -> HTTPMethod.values().first { it.name == m } }
	@Synchronized set(value) {
	  require(!didConnect)

	  val builder = cloner()
	  jCon = when (value) {
		GET    -> builder.GET().build()
		POST   -> builder.POST(publisher()).build()
		PUT    -> builder.PUT(publisher()).build()
		PATCH  -> builder.method(PATCH.name, publisher()).build()
		DELETE -> builder.DELETE().build()
	  }


	}
  actual var timeout: Duration?
	@Synchronized get() = jCon.timeout().getOrNull()?.toKotlinDuration()
	@Synchronized set(value) {
	  require(!didConnect)
	  jCon = cloner()
		.timeout(value?.toJavaDuration())
		.build()
	}


  @Synchronized override fun configureForWritingBytes(bytes: ByteArray) {
	require(!didConnect)
	val builder = cloner()
	jCon = builder.method(method.name, publisher()).build()
	outputDuringConnection {
	  BasicHTTPWriter(this, bytes).write()
	}
  }


  @Synchronized fun writeAsync(file: MFile) {
	require(!didConnect)
	outputDuringConnection {
	  AsyncWriter(this, file).write()
	}
  }


  private var didLiveConnectionOps = false
  private val liveConnectionOps = mutableListOf<JLiveHTTPConnection.()->Unit>()

  @Synchronized fun outputDuringConnection(op: JLiveHTTPConnection.()->Unit) {
	require(!didLiveConnectionOps)
	liveConnectionOps += op
  }


  private var didConnect = false

  @Synchronized actual override fun openConnection(): HTTPConnectResult {
	require(!didConnect)
	didConnect = true
	return try {

	  val client = HttpClient.newBuilder().build()
	  val response = client.sendAsync(jCon, BodyHandlers.ofInputStream())

	  if (liveConnectionOps.isNotEmpty()) {
		sync {
		  val liveConnection = JLiveHTTPConnection(outputStream)
		  liveConnectionOps.forEach {
			liveConnection.it()
		  }
		  liveConnection.outputStream.close()
		  didLiveConnectionOps = true
		}
	  }


	  JHTTPConnection(response.get())


	} catch (e: SocketTimeoutException) {
	  return Timeout
	} catch (e: ConnectException) {
	  if (e.message?.trim() == "Connection refused") return ConnectionRefused
	  else throw e
	}
  }

  @Synchronized override fun openAsyncConnection(): HTTPAsyncConnection {
	require(!didConnect)
	didConnect = true
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


