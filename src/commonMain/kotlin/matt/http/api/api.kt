package matt.http.api

import matt.http.connection.HTTPConnection
import matt.http.headers.HTTPHeaders
import matt.http.headers.headers
import matt.http.http
import matt.http.req.MutableHTTPRequest
import matt.http.url.MURL

interface API {
    suspend fun http(
        url: String,
        op: MutableHTTPRequest.() -> Unit = {}
    ): HTTPConnection
}

interface APIWithConfiguredHeaders : API {

    val urlPrefix: MURL

    val defaultHeaders: (HTTPHeaders.() -> Unit)? get() = null

    override suspend fun http(
        url: String,
        op: MutableHTTPRequest.() -> Unit
    ) = urlPrefix.resolve(url).http {
        headers {
            defaultHeaders?.invoke(this)
        }
        op()
    }

}