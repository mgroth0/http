package matt.http.api

import io.ktor.http.headers
import matt.http.connection.HTTPConnection
import matt.http.headers.HTTPHeaders
import matt.http.headers.auth.AuthHeader
import matt.http.headers.headers
import matt.http.http
import matt.http.req.MutableHTTPRequest
import matt.http.url.MURL
import matt.lang.jpy.ExcludeFromPython

fun MURL.asAPI(
    auths: Map<String, AuthHeader> = mapOf()
) = SimpleAPI(this, auths = auths)

class SimpleAPI(
    override val urlPrefix: MURL,
    auths: Map<String, AuthHeader> = mapOf()
) : AuthenticatedApi(auths = auths)

interface API {
    @ExcludeFromPython
    suspend fun http(
        url: String,
        op: MutableHTTPRequest.() -> Unit = {}
    ): HTTPConnection
}

abstract class APIWithConfiguredHeaders : API {

    @ExcludeFromPython
    abstract val urlPrefix: MURL

    @ExcludeFromPython
    abstract val defaultHeaders: (HTTPHeaders.() -> Unit)?

    @ExcludeFromPython
    final override suspend fun http(
        url: String,
        op: MutableHTTPRequest.() -> Unit
    ) = urlPrefix.resolve(url).http {
        headers {
            defaultHeaders?.invoke(this)
        }
        op()
    }
}

abstract class AuthenticatedApi(
    auths: Map<String, AuthHeader> = mapOf()
) : APIWithConfiguredHeaders() {
    final override val defaultHeaders: (HTTPHeaders.() -> Unit) = {
        headers {
            auths.forEach {
                setMySpecialBearerAuth(it.key, it.value)
            }
        }
    }
}

abstract class ConfiguredApi : APIWithConfiguredHeaders() {
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

abstract class SubApi(val path: String) : APIWithConfiguredHeaders() {
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


abstract class ThisHostBase : API {
    final override suspend fun http(
        url: String,
        op: MutableHTTPRequest.() -> Unit
    ): HTTPConnection = MURL(url).http(op = op)
}

object ThisHost : ThisHostBase()
