package matt.http.internet

import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection

//actual class TheInternet {
//
//}

actual fun TheInternet.isAvailable(): Boolean {
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