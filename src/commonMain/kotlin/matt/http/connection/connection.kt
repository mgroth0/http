package matt.http.connection


sealed interface HTTPConnectResult {
  fun requireSuccessful() = this as HTTPConnection
}

sealed interface HTTPConnectFailure: HTTPConnectResult

object Timeout: HTTPConnectFailure
object ConnectionRefused: HTTPConnectFailure

interface HTTPConnection: HTTPConnectResult {
  fun getRequestProperty(name: String): String?
  fun setRequestProperty(name: String, value: String?)
  fun print()
  val text: String
}