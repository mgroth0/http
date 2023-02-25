package matt.http.connection

import matt.lang.anno.SeeURL
import matt.lang.function.Consume
import matt.lang.go
import matt.log.warn.warn
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest

enum class ReadyState {
  UNSENT, OPENED, HEADERS_RECEIVED, LOADING, DONE
}

class JSHTTPConnection(private val xmlHttpRequest: XMLHttpRequest): HTTPResponse {


  @SeeURL("https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/response")
  override val bytes: ByteArray
	get() {
	  warn("JS response.bytes property still needs work. It is annoying.")
	  return text.encodeToByteArray()
	}
  override val text: String
	get() = xmlHttpRequest.responseText
  override val statusCode: Short
	get() = xmlHttpRequest.status


  //  val httpData by lazy {
  //	HTTPData(
  //	  readyState = ReadyState.values()[xmlHttpRequest.readyState.toInt()],
  //	  statusCode = xmlHttpRequest.status.toInt(),
  //	  statusText = xmlHttpRequest.statusText,
  //	  responseText = xmlHttpRequest.responseText
  //	)
  //  }


  val currentReadyState get() = ReadyState.values()[xmlHttpRequest.readyState.toInt()]
}


class JSHTTPAsyncConnection(private val xmlHttpRequest: XMLHttpRequest): HTTPAsyncConnection {
  override fun whenDone(op: Consume<HTTPConnectResult>) {
	doneData?.go {
	  op(it)
	} ?: run {
	  listeners += op
	}
  }

  private var doneData: HTTPConnectResult? = null
  private var listeners = mutableListOf<Consume<HTTPConnectResult>>()
  internal fun signalDone(data: HTTPConnectResult) {
	doneData = data
	listeners.forEach {
	  it(data)
	}
  }

}


internal fun XMLHttpRequest.setOnReadyStateChange(op: (Event)->Unit) {
  onreadystatechange = op
}
