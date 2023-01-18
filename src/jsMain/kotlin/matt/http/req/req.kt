package matt.http.req

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.file.FileOrURL
import matt.http.connection.HTTPConnectResult
import matt.http.connection.JSHTTPConnection
import matt.http.method.HTTPMethod
import matt.http.method.HTTPMethod.GET
import matt.http.resp.HTTPData
import matt.http.resp.ReadyState
import matt.http.resp.ReadyState.DONE
import matt.http.url.MURL
import matt.lang.anno.SeeURL
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


fun XMLHttpRequest.setOnReadyStateChange(op: (Event)->Unit) {
  onreadystatechange = op
}


class HTTPRequester<T>(
  private val type: HTTPMethod,
  private val url: FileOrURL,
  private val responses: HTTPData.()->T
) {

  @PublishedApi
  internal fun setup(async: Boolean) = XMLHttpRequest().apply {
	open(type.name, url.toString(), async = async)
  }

  @PublishedApi
  internal fun XMLHttpRequest.getResponses(): T = responses(
	HTTPData(
	  readyState = ReadyState.values()[readyState.toInt()],
	  statusCode = status.toInt(),
	  statusText = statusText,
	  responseText = responseText
	)
  )

  fun send(): T {
	val req = setup(async = false)
	req.send()
	return req.getResponses()
  }

  fun sendAsync(callback: (T)->Unit) {
	val req = setup(async = true)
	req.setOnReadyStateChange {
	  println("req.readyState=${req.readyState.toInt()}")
	  if (ReadyState.values()[req.readyState.toInt()] == DONE) {
		println("doing callback")
		callback(req.getResponses())
	  }
	}
	req.send()
  }

  inline fun <reified D> send(sendData: D): T {
	val req = setup(async = false)
	req.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
	req.send(Json.encodeToString(sendData))
	return req.getResponses()
  }

  inline fun <reified D> sendAsync(sendData: D, noinline callback: (T)->Unit) {
	val req = setup(async = true)
	req.setOnReadyStateChange {
	  if (ReadyState.values()[req.readyState.toInt()] == DONE) {
		callback(req.getResponses())
	  }
	}
	req.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
	req.send(Json.encodeToString(sendData))
  }
}

@SeeURL("https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest")
class JSHTTPRequest(
  private val xmlHttpRequest: XMLHttpRequest,
  private val url: MURL
): HTTPRequest {
  override var timeout: Duration?
	/*should be unsigned long, not int...*/
	get() = xmlHttpRequest.timeout.takeIf { it > 0 }?.milliseconds
	set(value) {
	  if (value == null) {
		xmlHttpRequest.timeout = 0
	  } else {
		xmlHttpRequest.timeout = value.inWholeMilliseconds.toInt()
	  }
	}
  override var method: HTTPMethod = GET


  override fun getRequestProperty(name: String): String? {
	return xmlHttpRequest.getResponseHeader(name)
  }

  @SeeURL("https://stackoverflow.com/questions/2464192/how-to-remove-http-specific-headers-in-javascript")
  override fun setRequestProperty(name: String, value: String?) {
	xmlHttpRequest.setRequestHeader(name, value ?: "")
  }


  fun connect(): HTTPConnectResult {
	xmlHttpRequest.open(method.name, url.toString(), async = false)
	return JSHTTPConnection(xmlHttpRequest)
  }
}



