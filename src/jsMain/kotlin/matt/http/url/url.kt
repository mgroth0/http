package matt.http.url

import matt.http.HTTPType.GET
import matt.http.req.HTTPRequester
import org.w3c.dom.url.URL


actual class MURL actual constructor(path: String): CommonURL {

  override val cpath = path

  private val jsURL = URL(path)

  actual val protocol: String get() = jsURL.protocol

  actual override fun resolve(other: String) = MURL(
	cpath.removeSuffix(CommonURL.URL_SEP) + CommonURL.URL_SEP + other.removePrefix(CommonURL.URL_SEP)
  )

  actual override fun toString() = cpath

  private val requester by lazy { HTTPRequester(type = GET, this) { responseText } }

  actual fun loadText() = requester.send()
}
