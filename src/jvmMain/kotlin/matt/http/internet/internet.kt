package matt.http.internet

import matt.lang.not
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection

class TheInternet {
  /*ugh, won't work with gradle daemon. There has to be a way to set properties that are reset for the gradle daemon*/
  val wasAvailableInThisRuntime by lazy {
	isAvailable()
  }
  val wasNotAvailableInThisRuntime get() = not(wasAvailableInThisRuntime)
  fun isNotAvailable() = not(isAvailable())
  fun isAvailable(): Boolean {
	return try {
	  val url = URL("http://www.google.com")
	  val conn: URLConnection = url.openConnection()
	  conn.connect()
	  conn.getInputStream().close()
	  true
	} catch (e: MalformedURLException) {
	  throw RuntimeException(e)
	} catch (e: IOException) {
	  false
	}
  }
}
