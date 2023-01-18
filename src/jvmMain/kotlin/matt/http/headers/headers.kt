package matt.http.headers

import matt.http.HTTPDslMarker
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import java.net.HttpURLConnection

@HTTPDslMarker
class HTTPHeaders internal constructor(private val con: HttpURLConnection) {


  var contentType: String by propProvider("Content-Type")
  var accept: String by propProvider("Accept")
  var auth: String by propProvider("Authorization")

  private fun propProvider(key: String) = provider {
	varProp(
	  getter = {
		con.getRequestProperty(key)
	  },
	  setter = {
		con.setRequestProperty(key, it)
	  }
	)
  }
}
