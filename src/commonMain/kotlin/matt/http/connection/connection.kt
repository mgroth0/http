package matt.http.connection


sealed interface HTTPConnectResult {
  fun requireSuccessful() = this as HTTPConnection
}

sealed interface HTTPConnectFailure: HTTPConnectResult

object Timeout: HTTPConnectFailure
object ConnectionRefused: HTTPConnectFailure

interface HTTPConnection: HTTPConnectResult {

  val text: String
  val statusCode: Int
}

fun HTTPConnection.print() {
  println(text)
}