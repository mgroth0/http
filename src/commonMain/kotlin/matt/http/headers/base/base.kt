package matt.http.headers.base

import matt.http.HTTPDslMarker
import matt.http.headers.key.HttpHeaderName
import matt.http.req.MutableHeaders
import matt.http.req.valueForHeader
import matt.lang.delegation.provider
import matt.lang.delegation.varProp


@HTTPDslMarker
abstract class HTTPHeadersBase internal constructor(
    @PublishedApi
    internal val con: MutableHeaders
) {

    fun <T: Any> setHeader(
        key: HttpHeaderName<T>,
        value: T
    ) {
        con.setHeader(key, value)
    }
    inline fun <reified T: Any> addHeaderNoDuplicates(
        key: HttpHeaderName<T>,
        value: T
    ) {
        if (con.valueForHeader(key) != value) {
            con.addHeader(key, value)
        }
    }

    operator fun <T: Any> set(
        key: HttpHeaderName<T>,
        value: T
    ) {
        setHeader(key, value)
    }


    fun removeAllHeaders(key: HttpHeaderName<*>) {
        con.removeAllHeaders(key)
    }


    internal inline fun <reified T: Any> propProvider(key: HttpHeaderName<T>) =
        provider {
            varProp(
                getter = { con.valueForHeader(key) },
                setter = {
                    if (it == null) {
                        removeAllHeaders(key)
                    } else {
                        setHeader(key, it)
                    }
                }
            )
        }
}
