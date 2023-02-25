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

@SeeURL("https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest")
actual class HTTPRequestImpl actual constructor(
  override val url: FileOrURL
): HTTPRequest() {

  private val xmlHttpRequest: XMLHttpRequest by lazy {
	XMLHttpRequest()
  }

  actual var timeout: Duration? = null
  actual override var method: HTTPMethod = GET
  private val requestHeaders = mutableMapOf<String, List<String>>()
  override fun getRequestProperty(name: String): String? {
	return requestHeaders[name]?.joinWithCommas()
  }

  actual override fun setRequestProperty(name: String, value: String?) {
	if (value == null) requestHeaders.remove(name)
	else requestHeaders[name] = value.split(",")
  }


  actual override fun allRequestHeaders(): Map<String, List<String>> = requestHeaders


  private var requestBody: ByteArray? = null

  override fun configureForWritingBytes(bytes: ByteArray) {
	requestBody = bytes
  }

  private fun send() {
	if (requestBody == null) xmlHttpRequest.send()
	else xmlHttpRequest.send(requestBody)
  }


  /*
  XmlHTTPRequest.open begins configuration but does not actually connect to anything. I don't think anything can be configured until after xmlHttpRequest.open
  */
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
	  if (ReadyState.values()[xmlHttpRequest.readyState.toInt()] == DONE) {
		asyncCon.signalDone(JSHTTPConnection(xmlHttpRequest))
	  }
	}
	send()
	return asyncCon
  }


}



