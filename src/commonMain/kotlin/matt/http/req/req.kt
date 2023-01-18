package matt.http.req

import matt.http.method.HTTPMethod
import kotlin.time.Duration

interface HTTPRequest {
  var timeout: Duration?
  /*method = PUT
  writeAsync(tarFile)
  verbose = true*/
  var method: HTTPMethod

  fun getRequestProperty(name: String): String?
  fun setRequestProperty(name: String, value: String?)
}