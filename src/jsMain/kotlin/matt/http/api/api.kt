package matt.http.api

import matt.http.connection.HTTPConnection
import matt.http.http
import matt.http.req.MutableHTTPRequest
import matt.http.url.MURL

abstract class ThisHostBase(): API {
    final override suspend fun http(
        url: String,
        op: MutableHTTPRequest.() -> Unit
    ): HTTPConnection = MURL(url).http(op = op)
}

object ThisHost : ThisHostBase()