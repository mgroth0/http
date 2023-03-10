package matt.http.api

import matt.http.connection.HTTPConnection
import matt.http.headers.HTTPHeaders
import matt.http.headers.headers
import matt.http.http
import matt.http.req.MutableHTTPRequest
import matt.http.url.MURL

interface API {
  suspend fun http(url: String, op: MutableHTTPRequest.()->Unit = {}): HTTPConnection
}

open class APIImpl(
  val urlPrefix: MURL
): API {

  open val defaultHeaders: (HTTPHeaders.()->Unit)? = null

  override suspend fun http(url: String, op: MutableHTTPRequest.()->Unit) = urlPrefix.resolve(url).http {
	headers {
	  defaultHeaders?.invoke(this)
	}
	op()
  }

}