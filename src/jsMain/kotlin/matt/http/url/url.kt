package matt.http.url

import matt.http.http
import org.w3c.dom.url.URL

external fun decodeURIComponent(encodedURI: String): String

actual class MURL actual constructor(path: String) : CommonURL {

    override val cpath = path

    private val jsURL by lazy { URL(path) }

    actual val protocol: String get() = jsURL.protocol

    actual override fun resolve(other: String) = MURL(
        cpath.removeSuffix(CommonURL.URL_SEP) + CommonURL.URL_SEP + other.removePrefix(CommonURL.URL_SEP)
    )

    actual override fun toString() = cpath

    /*private val requester by lazy { HTTPRequester(type = GET, this) { responseText } }*/

    actual suspend fun loadText() = http().text()
    /*requester.send()*/
}
