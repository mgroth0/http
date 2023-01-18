@file:JvmName("HttpJvmKt")

package matt.http

import matt.http.req.HTTPRequest
import matt.http.url.MURL
import java.net.URI
import java.net.URL



fun http(url: URI, op: HTTPRequest.()->Unit = {}) = MURL(url).http(op)

fun http(url: URL, op: HTTPRequest.()->Unit = {}) = MURL(url).http(op)


