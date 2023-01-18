package matt.http

import matt.http.connection.HTTPAsyncConnection
import matt.http.connection.HTTPConnectResult
import matt.http.req.HTTPRequestImpl
import matt.http.url.MURL
import kotlin.jvm.JvmName


@DslMarker
annotation class HTTPDslMarker


@JvmName("http1")
fun http(url: MURL, op: HTTPRequestImpl.()->Unit = {}) = url.http(op)

fun http(url: String, op: HTTPRequestImpl.()->Unit = {}) = MURL(url).http(op)



fun MURL.http(
  op: HTTPRequestImpl.()->Unit = {},
): HTTPConnectResult {
  val req = HTTPRequestImpl(this)
  req.op()
  return req.connectSync()
}


fun MURL.httpAsync(
  op: HTTPRequestImpl.()->Unit = {},
): HTTPAsyncConnection {
  val req = HTTPRequestImpl(this)
  req.op()
  return req.connectAsync()
}