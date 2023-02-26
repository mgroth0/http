package matt.http

import matt.file.FileOrURL
import matt.http.connection.HTTPConnection
import matt.http.req.MutableHTTPRequest
import matt.http.req.requester.HTTPRequester
import matt.http.url.MURL
import kotlin.jvm.JvmName


@DslMarker
annotation class HTTPDslMarker

@JvmName("http1")
suspend fun http(url: FileOrURL, op: MutableHTTPRequest.()->Unit = {}) = url.http(op = op)
suspend fun http(url: String, op: MutableHTTPRequest.()->Unit = {}) = MURL(url).http(op = op)

suspend fun FileOrURL.http(
  requester: HTTPRequester = HTTPRequester.DEFAULT,
  op: MutableHTTPRequest.()->Unit = {},
): HTTPConnection {
  val req = MutableHTTPRequest()
  req.url = this.cpath
  req.op()
  val snap = req.snapshot()
  return requester.copy(request = snap).sendAndThrowUnlessConnectedCorrectly()
}