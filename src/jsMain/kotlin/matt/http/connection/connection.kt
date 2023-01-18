package matt.http.connection

import org.w3c.xhr.XMLHttpRequest

class JSHTTPConnection(private val xmlHttpRequest: XMLHttpRequest): HTTPConnection {



  override val text: String
	get() = xmlHttpRequest.responseText
  override val statusCode: Int
	get() = xmlHttpRequest.status.toInt()
}
