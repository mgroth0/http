@file:JvmName("HttpJvmKt")

package matt.http

import matt.http.connection.HTTPConnectResult
import matt.http.req.JHTTPRequest
import matt.http.url.MURL
import java.net.URI
import java.net.URL

@JvmName("http1")
fun http(url: MURL, op: JHTTPRequest.()->Unit = {}) = url.http(op)

fun http(url: URI, op: JHTTPRequest.()->Unit = {}) = MURL(url).http(op)

fun http(url: URL, op: JHTTPRequest.()->Unit = {}) = MURL(url).http(op)

fun http(url: String, op: JHTTPRequest.()->Unit = {}) = MURL(url).http(op)

fun MURL.http(
  op: JHTTPRequest.()->Unit = {},
): HTTPConnectResult {
  val req = JHTTPRequest(this)
  req.op()
  return req.connect()
}

