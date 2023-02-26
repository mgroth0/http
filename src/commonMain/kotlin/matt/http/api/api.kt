package matt.http.api

import matt.http.headers.HTTPHeaders
import matt.http.headers.headers
import matt.http.http
import matt.http.req.MutableHTTPRequest
import matt.http.url.MURL

open class API(
  val urlPrefix: MURL
) {

  open val defaultHeaders: (HTTPHeaders.()->Unit)? = null

  suspend fun http(url: String, op: MutableHTTPRequest.()->Unit = {}) = urlPrefix.resolve(url).http {
	headers {
	  defaultHeaders?.invoke(this)
	}
	op()
  }

}