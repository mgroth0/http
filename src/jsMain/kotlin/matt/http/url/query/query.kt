package matt.http.url.query

import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

fun getURLQueryParam(p: String) = getURLQueryParamOrNull(p)!!
fun getURLQueryParamOrNull(p: String) = URLSearchParams(window.location.href.substringAfter("?")).get(p)

fun <Q: QueryParams> Q.fillValuesFromCurrentUrl(): Q {
    params.forEach {
        it.value = getURLQueryParamOrNull(it.name)
    }
    return this
}