package matt.http.url.query

import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

fun getURLQueryParam(p: String) = URLSearchParams(window.location.href.substringAfter("?")).getAll(p)

fun <Q: QueryParams> Q.fillValuesFromCurrentUrl(): Q {
    params.forEach {
        it.value = getURLQueryParam(it.name).toList()
    }
    return this
}
