package matt.http.req

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.file.FileOrURL
import matt.http.method.HTTPMethod
import matt.http.resp.HTTPData
import matt.http.resp.ReadyState
import matt.http.resp.ReadyState.DONE
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest


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

class JSHTTPRequest(xmlHttpRequest: XMLHttpRequest): HTTPRequest {

}



