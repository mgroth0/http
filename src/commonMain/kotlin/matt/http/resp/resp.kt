package matt.http.resp


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