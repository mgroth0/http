@file:JvmName("InternetJvmAndroidKt")


package matt.http.internet

import matt.lang.url.toURL
import java.io.IOException
import java.net.MalformedURLException
import java.net.URLConnection

actual fun TheInternet.isAvailable(): Boolean = try {
    val url = "http://www.google.com".toURL()
    val conn: URLConnection = url.openConnection()
    conn.connect()
    conn.getInputStream().close()
    true
} catch (e: MalformedURLException) {
    throw RuntimeException(e)
} catch (e: IOException) {
    false
}
