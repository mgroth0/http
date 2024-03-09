package matt.http.url.query.js

import kotlinx.browser.window
import matt.http.url.query.QueryParams
import matt.lang.common.substringAfterSingular
import org.w3c.dom.url.URLSearchParams

fun getURLQueryParam(p: String) = URLSearchParams(window.location.href.substringAfterSingular("?")).getAll(p)

fun <Q: QueryParams> Q.fillValuesFromCurrentUrl(): Q {
    params.forEach {
        it.value = getURLQueryParam(it.name).toList()
    }
    return this
}
