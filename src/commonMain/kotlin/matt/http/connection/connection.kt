package matt.http.connection

import matt.lang.function.Consume


sealed interface HTTPConnectResult {
  fun requireSuccessful() = this as HTTPResponse
}

sealed interface HTTPConnectFailure: HTTPConnectResult

object Timeout: HTTPConnectFailure
object ConnectionRefused: HTTPConnectFailure

interface HTTPResponse: HTTPConnectResult {

  val text: String
  val statusCode: Int
  val statusMessage: String
}

fun HTTPResponse.print() {
  println(text)
}


interface HTTPAsyncConnection {
  fun whenDone(op: Consume<HTTPConnectResult>)
}