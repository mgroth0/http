package matt.http.connection

import matt.lang.anno.SeeURL
import matt.lang.function.Consume


sealed interface HTTPConnectResult {
  @SeeURL("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status")
  fun requireSuccessful() = (this as HTTPResponse).apply {
	when (statusCode) {
	  in 300..399 -> throw RedirectionException(statusCode, text)
	  in 400..499 -> throw when (statusCode.toInt()) {
		401  -> UnauthorizedException(text)
		else -> ClientErrorException(statusCode, text)
	  }
	  in 500..599 -> throw ServerErrorException(statusCode, text)
	}
	require(statusCode in 100..300) {
	  "weird status code: $statusCode"
	}
  }
}




sealed class HTTPProblemException(status: Short, message: String): Exception("$status: ${message}")
class RedirectionException(status: Short, message: String): HTTPProblemException(status, message)
sealed class HTTPErrorException(status: Short, message: String): HTTPProblemException(status, message)
open class ClientErrorException(status: Short, message: String): HTTPErrorException(status, message)
class UnauthorizedException(message: String): ClientErrorException(401, message)
class ServerErrorException(status: Short, message: String): HTTPErrorException(status, message)

sealed interface HTTPConnectFailure: HTTPConnectResult

object Timeout: HTTPConnectFailure
object ConnectionRefused: HTTPConnectFailure

interface HTTPResponse: HTTPConnectResult {
  val bytes: ByteArray
  val text: String
  val statusCode: Short
}

fun HTTPResponse.print() {
  println(text)
}


interface HTTPAsyncConnection {
  fun whenDone(op: Consume<HTTPConnectResult>)
}