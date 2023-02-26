@file:JvmName("HttpJvmKt")

package matt.http

import matt.http.req.MutableHTTPRequest
import matt.http.req.requester.HTTPRequester
import matt.http.url.MURL
import java.net.URI
import java.net.URL


suspend fun http(url: URI, op: MutableHTTPRequest.()->Unit = {}) = MURL(url).http(op=op)

suspend fun http(
  url: URL,
  requester: HTTPRequester = HTTPRequester.DEFAULT,
  op: MutableHTTPRequest.()->Unit = {}
) = MURL(url).http(requester = requester, op)


