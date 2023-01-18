@file:JvmName("HttpJvmKt")

package matt.http

import matt.http.req.HTTPRequestImpl
import matt.http.url.MURL
import java.net.URI
import java.net.URL



fun http(url: URI, op: HTTPRequestImpl.()->Unit = {}) = MURL(url).http(op)

fun http(url: URL, op: HTTPRequestImpl.()->Unit = {}) = MURL(url).http(op)


