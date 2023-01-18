package matt.http.req

import matt.http.connection.HTTPConnectResult
import matt.http.method.HTTPMethod
import matt.http.url.MURL
import kotlin.time.Duration

expect class HTTPRequest internal constructor(url: MURL) {
  var timeout: Duration?

  /*method = PUT
  writeAsync(tarFile)
  verbose = true*/
  var method: HTTPMethod

  fun getRequestProperty(name: String): String?
  fun setRequestProperty(name: String, value: String?)

  internal fun connect(): HTTPConnectResult
}