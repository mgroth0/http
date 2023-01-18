package matt.http.resp

import matt.http.resp.ReadyState.DONE


enum class ReadyState {
  UNSENT, OPENED, HEADERS_RECEIVED, LOADING, DONE
}

open class HTTPResponse(
  val readyState: ReadyState,
)


sealed interface ServerResponse
sealed interface Success: ServerResponse
open class SuccessText(val text: String?): Success {
  override fun toString() = SuccessText::class.simpleName!!
}

object SimpleSuccess: SuccessText(null) {
  override fun toString() = SimpleSuccess::class.simpleName!!
}

class Failure(val status: Int, val message: String): ServerResponse {
  override fun toString(): String {
	return "$status: $message"
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