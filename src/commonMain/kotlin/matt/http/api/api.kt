package matt.http.api

import matt.http.headers.HTTPHeaders
import matt.http.http
import matt.http.httpAsync
import matt.http.req.HTTPRequestImpl
import matt.http.req.headers
import matt.http.url.MURL

open class API(
  val urlPrefix: MURL
) {

  fun http(url: String, op: HTTPRequestImpl.()->Unit = {}) = urlPrefix.resolve(url).http {
	headers {
	  defaultHeaders?.invoke(this)
	}
	op()
  }

  fun httpAsync(url: String, op: HTTPRequestImpl.()->Unit = {}) = urlPrefix.resolve(url).httpAsync {
	headers {
	  defaultHeaders?.invoke(this)
	}
	op()
  }

  open val defaultHeaders: (HTTPHeaders.()->Unit)? = null

}