@file:JvmName("HttpJvmKt")

package matt.http

import matt.file.commons.USER_HOME
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection

fun netIsAvailable(): Boolean {
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


fun httpPost(
  url: String, headers: Map<String, String>, data: String
): ByteArray {


  val con: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
  con.requestMethod = "POST"


  headers.forEach {
	con.setRequestProperty(it.key, it.value)
  }

  con.doOutput = true

  con.outputStream.write(data.encodeToByteArray())

  return con.inputStream.readAllBytes()

}


object NetRC {
  private val netrc = USER_HOME[".netrc"]
  private val lines = netrc.readText().lines().map { it.trim() }
  val login = lines.first { it.startsWith("login") }.substringAfter("login").trim()
  val password = lines.first { it.startsWith("password") }.substringAfter("password").trim()
}