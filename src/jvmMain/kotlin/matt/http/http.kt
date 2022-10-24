@file:JvmName("HttpJvmKt")

package matt.http

import matt.file.commons.USER_HOME
import matt.http.url.MURL
import matt.log.tab
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


fun http(
  method: HTTPType,
  url: MURL,
  headers: Map<String, String> = mapOf(),
  data: ByteArray? = null,
  printResponse: Boolean = false
): ByteArray {


  val con: HttpURLConnection = url.jURL.openConnection() as HttpURLConnection
  con.requestMethod = method.name



  headers.forEach {
	con.setRequestProperty(it.key, it.value)
  }




  println("sending ${method.name} to $url")
  println("request properties:")
  con.requestProperties.forEach {
	tab("${it.key}:${it.value}")
  }

  data?.let {
	con.doOutput = true
	con.outputStream.write(data)
  }


  println("Response = ${con.responseCode}")
  println("Message = ${con.responseMessage}")



  con.errorStream?.readAllBytes()?.let {

	/*
	*
	*   * <p>This method will not cause a connection to be initiated.  If
    * the connection was not connected, or if the server did not have
    * an error while connecting or if the server had an error but
    * no error data was sent, this method will return null. This is
    * the default.
    *
    * @return an error stream if any, null if there have been no
    * errors, the connection is not connected or the server sent no
    * useful data.
	* */

	println("ERR: ${it.decodeToString()}")
  } ?: run {
	if (printResponse) {
	  println("no error message from server")
	}
  }

  return con.inputStream.readAllBytes().also {
	if (printResponse) {
	  println("RESPONSE: ${it.decodeToString()}")
	}
  }

}


object NetRC {
  private val netrc = USER_HOME[".netrc"]
  private val lines = netrc.readText().lines().map { it.trim() }
  val login = lines.first { it.startsWith("login") }.substringAfter("login").trim()
  val password = lines.first { it.startsWith("password") }.substringAfter("password").trim()
}