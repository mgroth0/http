package matt.http.url

import matt.http.http
import matt.http.url.common.CommonUrl
import matt.http.url.common.UrlResolver
import org.w3c.dom.url.URL

private external fun decodeURIComponent(encodedURI: String): String

actual class MURL actual constructor(actual override val path: String) : CommonUrl<MURL>, UrlResolver<MURL> {

    private val jsURL by lazy { URL(path) }

    actual val protocol: String get() = jsURL.protocol

    actual override fun resolve(other: String) =
        MURL(
            path.removeSuffix(CommonUrl.URL_SEP) + CommonUrl.URL_SEP + other.removePrefix(CommonUrl.URL_SEP)
        )

    actual override fun toString() = path

    /*private val requester by lazy { HTTPRequester(type = GET, this) { responseText } }*/

    actual suspend fun loadText() = http().text()
    /*requester.send()*/

    actual override fun plus(other: String): MURL = resolve(other)


    actual override fun get(item: String): MURL = resolve(item)
}
