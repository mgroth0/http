package matt.http

import matt.http.connection.HTTPConnectResult
import matt.http.req.HTTPRequest
import matt.http.url.MURL
import kotlin.jvm.JvmName


@DslMarker
annotation class HTTPDslMarker


@JvmName("http1")
fun http(url: MURL, op: HTTPRequest.()->Unit = {}) = url.http(op)

fun http(url: String, op: HTTPRequest.()->Unit = {}) = MURL(url).http(op)



fun MURL.http(
  op: HTTPRequest.()->Unit = {},
): HTTPConnectResult {
  val req = HTTPRequest(this)
  req.op()
  return req.connect()
}

