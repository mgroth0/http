package matt.http.api

import matt.http.connection.HTTPConnection
import matt.http.headers.HTTPHeaders
import matt.http.headers.headers
import matt.http.http
import matt.http.req.MutableHTTPRequest
import matt.http.url.MURL
import matt.lang.jpy.ExcludeFromPython

fun MURL.asAPI() = SimpleAPI(this)
class SimpleAPI(override val urlPrefix: MURL) : APIWithConfiguredHeaders

interface API {
    @ExcludeFromPython
    suspend fun http(
        url: String,
        op: MutableHTTPRequest.() -> Unit = {}
    ): HTTPConnection


}

interface APIWithConfiguredHeaders : API {

    @ExcludeFromPython
    val urlPrefix: MURL

    @ExcludeFromPython
    val defaultHeaders: (HTTPHeaders.() -> Unit)? get() = null

    @ExcludeFromPython
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

abstract class ConfiguredApi : APIWithConfiguredHeaders {
    @ExcludeFromPython
    protected abstract val parentApi: APIWithConfiguredHeaders

    @ExcludeFromPython
    final override val urlPrefix get() = parentApi.urlPrefix

    @ExcludeFromPython
    protected open val subHeaders: (HTTPHeaders.() -> Unit)? get() = null

    @ExcludeFromPython
    final override val defaultHeaders: HTTPHeaders.() -> Unit
        get() = {
            val def = parentApi.defaultHeaders
            def?.invoke(this)
            subHeaders?.invoke(this)
        }

}

abstract class SubApi(val path: String) : APIWithConfiguredHeaders {
    @ExcludeFromPython
    protected abstract val parentApi: APIWithConfiguredHeaders

    @ExcludeFromPython
    final override val urlPrefix get() = parentApi.urlPrefix.resolve(path)

    @ExcludeFromPython
    protected open val subHeaders: (HTTPHeaders.() -> Unit)? get() = null

    @ExcludeFromPython
    final override val defaultHeaders: HTTPHeaders.() -> Unit
        get() = {
            val def = parentApi.defaultHeaders
            def?.invoke(this)
            subHeaders?.invoke(this)
        }

}