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
import matt.http.method.HTTPMethod.DELETE
import matt.http.method.HTTPMethod.GET
import matt.http.method.HTTPMethod.PATCH
import matt.http.method.HTTPMethod.POST
import matt.http.method.HTTPMethod.PUT
import matt.http.req.write.AsyncWriter
import matt.http.req.write.BasicHTTPWriter
import matt.lang.sync
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.util.function.BiPredicate
import java.util.function.Supplier
import kotlin.concurrent.thread
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration


/*typealias SunHTTPConnection = sun.net.www.protocol.http.HttpURLConnection*/

actual class HTTPRequestImpl internal actual constructor(override val url: FileOrURL): HTTPRequest() {


  /*  companion object {
	  @SeeURL("https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch")
	  private fun allowMethods(vararg methods: String) {
		try {
		  val methodsField: Field = HttpURLConnection::class.java.getDeclaredField("methods")
		  val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
		  modifiersField.isAccessible = true
		  modifiersField.setInt(methodsField, methodsField.modifiers and Modifier.FINAL.inv())
		  methodsField.isAccessible = true
		  @Suppress("UNCHECKED_CAST") val oldMethods = methodsField.get(null) as Array<String>
		  val oldMethodsAsList = Arrays.asList(*oldMethods).toMutableList()
		  val methodsSet: LinkedHashSet<String> = LinkedHashSet(oldMethodsAsList)
		  val methodsList = methods.toList()
		  methodsSet.addAll(methodsList)
		  val newMethods = methodsSet.toTypedArray<String>()
		  methodsField.set(null *//*static field*//*, newMethods)
	  } catch (e: NoSuchFieldException) {
		throw IllegalStateException(e)
	  } catch (e: IllegalAccessException) {
		throw IllegalStateException(e)
	  }
	}

	init {
	  allowMethods("PATCH")
	}
  }*/ //  init {
  //	println("configuring connection for ${url.cpath}")
  //	Thread.dumpStack()
  //  }


  /*https://www.baeldung.com/java-9-http-client*//*private val jCon = URI(url.cpath).toURL().openConnection() as HttpURLConnection*/
  private var jCon = HttpRequest.newBuilder().uri(URI(url.cpath)).GET().build()

  /*private var con = JHTTPConnection(jCon)*/


  @Synchronized override fun getRequestProperty(name: String): String? {
	return jCon.headers().map()[name]?.single()	/*return jCon.getRequestProperty(name)*/
  }

  @Synchronized actual override fun setRequestProperty(name: String, value: String?) {
	require(!didConnect)
	if (value == null) {
	  jCon = HttpRequest.newBuilder(jCon, BiPredicate { k, _ -> k != name }).build()
	} else {
	  jCon = HttpRequest.newBuilder(jCon, BiPredicate { _, _ -> true }).header(name, value).build()
	}


	/*
		if (value == null) {
		  jCon.headers().map().remove(name)
		} else {
		  val li = jCon.headers().map()[name]
		  if (li == null) {
			jCon.headers().map()[name] = listOf(value)
		  } else {
			require(li.isEmpty())
			li.add(value)
		  }

		}
	*/

	/*println("setting request property $name to $value")*/	/*jCon.setRequestProperty(name, value)*/
  }

  @Synchronized/*actual override fun allRequestHeaders() = jCon.requestProperties*/
  actual override fun allRequestHeaders() = jCon.headers().map().toMap()


  private val writeStreams by lazy {
	val output = PipedOutputStream()
	val input = PipedInputStream(output)
	input to output
  }

  private val outputStream by lazy { writeStreams.second }


  actual override var method: HTTPMethod
	/*get() = jCon.requestMethod.let { m -> HTTPMethod.values().first { it.name == m } }*/
	@Synchronized get() = jCon.method().let { m -> HTTPMethod.values().first { it.name == m } }
	@Synchronized set(value) {
	  require(!didConnect)


	  /*	  if (value == PATCH) {

			  println("hacking method of ${jCon} to ${value}")

			  val lockFun = SunHTTPConnection::class.java.getDeclaredMethod("lock")
			  lockFun.isAccessible = true
			  lockFun.invoke(jCon)


			  val connectedField = URLConnection::class.java.getDeclaredField("connected")
			  connectedField.isAccessible = true
			  if (connectedField.get(jCon) as Boolean) {
				error("already connected")
			  }

			  println("I should start carefully migrating to the newer java http code")
			  val methodField = HttpURLConnection::class.java.getDeclaredField("method")

			  methodField.isAccessible = true
			  methodField.set(jCon, value.name)

			  val unlockFun = SunHTTPConnection::class.java.getDeclaredMethod("unlock")
			  unlockFun.isAccessible = true
			  unlockFun.invoke(jCon)

			  println("hacked method of ${jCon} to ${value}")
			  println("now method of ${jCon} is ${jCon.requestMethod}")

			} else {
			  jCon.requestMethod = value.name
			}*/

	  val builder = HttpRequest.newBuilder(jCon, BiPredicate { _, _ -> true })
	  fun publisher() = BodyPublishers.ofInputStream(Supplier { writeStreams.first })
	  jCon = when (value) {
		GET    -> builder.GET().build()
		POST   -> builder.POST(publisher()).build()
		PUT    -> builder.PUT(publisher()).build()
		PATCH  -> builder.method("PATCH", publisher()).build()
		DELETE -> builder.DELETE().build()
	  }	/*con = JHTTPConnection(jCon)*/


	}
  actual var timeout: Duration?
	/*get() = jCon.connectTimeout.takeIf { it != 0 }?.let {
	  require(it > 0)
	  it.milliseconds
	}*/
	@Synchronized get() = jCon.timeout().getOrNull()?.toKotlinDuration()
	@Synchronized set(value) {
	  require(!didConnect)
	  jCon = HttpRequest.newBuilder(jCon, BiPredicate { _, _ -> true })
		.timeout(value?.toJavaDuration())
		.build()	/*con = JHTTPConnection(jCon)*/	/*jCon.connectTimeout = value?.inWholeMilliseconds?.toInt() ?: 0*/
	}

  @Synchronized fun writeAsync(file: MFile) {
	require(!didConnect)
	outputDuringConnection {
	  AsyncWriter(this, file).write()
	}
  }

  @Synchronized override fun configureForWritingBytes(bytes: ByteArray) {
	require(!didConnect)
	val builder = HttpRequest.newBuilder(jCon, BiPredicate { _, _ -> true })
	fun publisher() = BodyPublishers.ofInputStream(Supplier { writeStreams.first })
	jCon = builder.method(method.name, publisher()).build()
	outputDuringConnection {
	  BasicHTTPWriter(this, bytes).write()
	}
  }


  private var didLiveConnectionOps = false
  private val liveConnectionOps = mutableListOf<JLiveHTTPConnection.()->Unit>()

  @Synchronized fun outputDuringConnection(op: JLiveHTTPConnection.()->Unit) {
	require(!didLiveConnectionOps)	/*jCon.doOutput = true*/
	liveConnectionOps += op
  }


  private var didConnect = false

  @Synchronized actual override fun openConnection(): HTTPConnectResult {	//	println("connect 1: $url")
	require(!didConnect)
	didConnect = true
	return try {

	  //	  println("connect 2: $url")
	  val client = HttpClient.newBuilder()

		.build()

	  /*println("THE METHOD 1 for ${jCon.url} is ${jCon.requestMethod}")*/	//	  var response: HttpResponse.ResponseInfo? = null


	  //	  println("connect 3: $url")
	  val response = client.sendAsync(jCon, BodyHandlers.ofInputStream())	//	  println("connect 4: $url")
	  /*jCon.connect()*/

	  /*println("THE METHOD 2 for ${jCon.url} is ${jCon.requestMethod}")*/



	  if (liveConnectionOps.isNotEmpty()) {
		sync {
		  val liveConnection = JLiveHTTPConnection(outputStream)
		  liveConnectionOps.forEach {
			liveConnection.it()
		  }
		  liveConnection.outputStream.close()
		  didLiveConnectionOps = true
		}
	  }	//	  println("connect 5: $url")


	  JHTTPConnection(response.get())	/*con*/


	} catch (e: SocketTimeoutException) {
	  println("Timeout!")
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


