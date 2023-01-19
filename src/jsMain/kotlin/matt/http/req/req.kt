package matt.http.req

import matt.file.FileOrURL
import matt.http.connection.HTTPAsyncConnection
import matt.http.connection.HTTPConnectResult
import matt.http.connection.JSHTTPAsyncConnection
import matt.http.connection.JSHTTPConnection
import matt.http.connection.ReadyState
import matt.http.connection.ReadyState.DONE
import matt.http.connection.setOnReadyStateChange
import matt.http.method.HTTPMethod
import matt.http.method.HTTPMethod.GET
import matt.lang.anno.SeeURL
import matt.lang.go
import matt.prim.str.joinWithCommas
import org.w3c.xhr.XMLHttpRequest
import kotlin.time.Duration


//@Deprecated("switch to stuff in common")
//class HTTPRequester<T>(
//  private val type: HTTPMethod,
//  private val url: FileOrURL,
//  private val responses: HTTPData.()->T
//) {

  //  @PublishedApi
  //  internal fun setup(async: Boolean) = XMLHttpRequest().apply {
  //	open(type.name, url.toString(), async = async)
  //  }

  //  @PublishedApi
  //  internal fun XMLHttpRequest.getResponses(): T = responses(
  //	HTTPData(
  //	  readyState = ReadyState.values()[readyState.toInt()],
  //	  statusCode = status.toInt(),
  //	  statusText = statusText,
  //	  responseText = responseText
  //	)
  //  )

  //  fun send(): T {
  //	val req = setup(async = false)
  //	req.send()
  //	return req.getResponses()
  //  }

//  fun sendAsync(callback: (T)->Unit) {
//	val req = setup(async = true)
//	req.setOnReadyStateChange {
//	  println("req.readyState=${req.readyState.toInt()}")
//	  if (ReadyState.values()[req.readyState.toInt()] == DONE) {
//		println("doing callback")
//		callback(req.getResponses())
//	  }
//	}
//	req.send()
//  }

  //  inline fun <reified D> send(sendData: D): T {
  //	val req = setup(async = false)
  //	req.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
  //	req.send(Json.encodeToString(sendData))
  //	return req.getResponses()
  //  }

//  inline fun <reified D> sendAsync(sendData: D, noinline callback: (T)->Unit) {
//	val req = setup(async = true)
//	req.setOnReadyStateChange {
//	  if (ReadyState.values()[req.readyState.toInt()] == DONE) {
//		callback(req.getResponses())
//	  }
//	}
//	req.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
//	req.send(Json.encodeToString(sendData))
//  }


//}

@SeeURL("https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest")
actual class HTTPRequestImpl actual constructor(
  override val url: FileOrURL
): HTTPRequest() {

  private val xmlHttpRequest: XMLHttpRequest by lazy {
	XMLHttpRequest()
  }

  actual var timeout: Duration? = null

  /*	*//*should be unsigned long, not int...*//*
	get() = xmlHttpRequest.timeout.takeIf { it > 0 }?.milliseconds
	set(value) {
	  if (value == null) {
		xmlHttpRequest.timeout = 0
	  } else {
		xmlHttpRequest.timeout = value.inWholeMilliseconds.toInt()
	  }
	}*/
  actual override var method: HTTPMethod = GET


  private val requestHeaders = mutableMapOf<String, List<String>>()


  override fun getRequestProperty(name: String): String? {
	return requestHeaders[name]?.joinWithCommas()
  }

  //  @SeeURL("https://stackoverflow.com/questions/2464192/how-to-remove-http-specific-headers-in-javascript")
  actual override fun setRequestProperty(name: String, value: String?) {
	if (value == null) {
	  requestHeaders.remove(name)
	} else {
	  requestHeaders[name] = value.split(",")
	}
  }


  @SeeURL("https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/getAllResponseHeaders")
  actual override fun allRequestHeaders(): Map<String, List<String>> {
	return requestHeaders
	/*	val all = xmlHttpRequest.getRe()
		val r = mutableMapOf<String, MutableList<String>>()
		all.lines().filter { it.isNotBlank() }.forEach {
		  val k = it.substringBefore(":").trim()
		  val v = it.substringAfter(":").trim()
		  val l = r.getOrPut(k) { mutableListOf() }
		  l.add(v)
		}
		return r*/
  }


  private var requestBody: ByteArray? = null

  override fun configureForWritingBytes(bytes: ByteArray) {
	requestBody = bytes
  }

  private fun send() {
	if (requestBody == null) xmlHttpRequest.send()
	else xmlHttpRequest.send(requestBody)
  }

  //  actual override fun writeBytesNow(bytes: ByteArray) {
  //	/*NOTE: XMLHttpRequest.send behaves differently depending on if async is true or false*/
  //	@SeeURL("https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/send")
  //	xmlHttpRequest.send(bytes)
  //  }



  /*XmlHTTPRequest.open begins configuration but does not actually connect to anything. I don't think anything can be configured until after xmlHttpRequest.open*/
  fun configure(async: Boolean) {
	@SeeURL("https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/open")
	@SeeURL("https://udn.realityripple.com/docs/Web/API/XMLHttpRequest/timeout")
	require(timeout == null) {
	  "According to UDN web docs: \"Timeout shouldn't be used for synchronous XMLHttpRequests requests used in a document environment or it will throw an InvalidAccessError exception\""
	}
	xmlHttpRequest.open(method.name, url.cpath, async = async)
	requestHeaders.entries.forEach {
	  xmlHttpRequest.setRequestHeader(it.key, it.value.joinWithCommas())
	}
	timeout?.inWholeMilliseconds?.toInt()?.go {
	  xmlHttpRequest.timeout = it
	}

  }

  actual override fun openConnection(): HTTPConnectResult {
	configure(async = false)
	send()
	require(xmlHttpRequest.status.toInt() == 200) {
	  "I have no idea what to do when synchronous XMLHttpRequests are not successful. The online documentation for this is case is sparse and basically all say that synchronous XMLHttpRequests are deprecated. So probably should just switch to async"
	}
	return JSHTTPConnection(xmlHttpRequest)
  }


  override fun openAsyncConnection(): HTTPAsyncConnection {
	configure(async = true)
	val asyncCon = JSHTTPAsyncConnection(xmlHttpRequest)
	xmlHttpRequest.setOnReadyStateChange {
	  println("req.readyState=${xmlHttpRequest.readyState.toInt()}")
	  if (ReadyState.values()[xmlHttpRequest.readyState.toInt()] == DONE) {
		println("doing callback")
		asyncCon.signalDone(JSHTTPConnection(xmlHttpRequest))
	  }
	}
	send()
	return asyncCon
  }


}



