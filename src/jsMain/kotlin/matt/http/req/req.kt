package matt.http.req

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.file.FileOrURL
import matt.http.method.HTTPMethod
import matt.http.resp.HTTPResponse
import matt.http.resp.ReadyState
import matt.http.resp.ReadyState.DONE
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest


fun XMLHttpRequest.setOnReadyStateChange(op: (Event)->Unit) {
  onreadystatechange = op
}


//val XMLHttpRequest.readyState get() = matt.http.resp.ReadyState.values()[readyState.toInt()]

class HTTPRequester<T>(
  private val type: HTTPMethod,
  private val url: FileOrURL,
  private val responses: HTTPData.()->T
) {

  @Suppress("FunctionName")
  fun _setup(async: Boolean) = XMLHttpRequest().apply {
	open(type.name, url.toString(), async = async)
  }

  @Suppress("FunctionName")
  fun XMLHttpRequest._getResponses(): T = responses(
	HTTPData(
	  readyState = ReadyState.values()[readyState.toInt()],
	  statusCode = status.toInt(),
	  statusText = statusText,
	  responseText = responseText
	)
  )

  fun send(): T {
	val req = _setup(async = false)
	req.send()
	return req._getResponses()
  }

  fun sendAsync(callback: (T)->Unit) {
	val req = _setup(async = true)
	req.setOnReadyStateChange {
	  println("req.readyState=${req.readyState.toInt()}")
	  if (ReadyState.values()[req.readyState.toInt()] == DONE) {
		println("doing callback")
		callback(req._getResponses())
	  }
	}
	req.send()
  }

  inline fun <reified D> send(sendData: D): T {
	val req = _setup(async = false)
	req.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
	req.send(Json.encodeToString(sendData))
	return req._getResponses()
  }

  inline fun <reified D> sendAsync(sendData: D, noinline callback: (T)->Unit) {
	val req = _setup(async = true)
	req.setOnReadyStateChange {
	  if (ReadyState.values()[req.readyState.toInt()] == DONE) {
		callback(req._getResponses())
	  }
	}
	req.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
	req.send(Json.encodeToString(sendData))
  }


}



open class HTTPHeaders(
  readyState: ReadyState,
  val statusCode: Int,
  val statusText: String,
): HTTPResponse(readyState = readyState)

class HTTPData(
  readyState: ReadyState,
  statusCode: Int,
  statusText: String,
  val responseText: String
): HTTPHeaders(readyState = readyState, statusCode = statusCode, statusText = statusText) {
  init {
	require(readyState == DONE)
  }
}