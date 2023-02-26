package matt.http.live

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream

/*HttpURLConnection*/
class JLiveHTTPConnection(private val jCon: OutputStream): LiveHTTPConnection {

  internal val outputStream get() = jCon/*.outputStream*/

  fun writeBytesNow(bytes: ByteArray) {

	//	jCon.doOutput = true
	println("trying to get jCon output stream...")
	try {
	  val oStream = jCon/*.outputStream*/
	  println("got ostream. Writing...")
	  oStream.write(bytes)
	  println("wrote!")
	} catch (e: Exception) {
	  println("GOT EXCEPTION")
	  throw e
	}


  }
  fun writeStringNow(string: String) = writeBytesNow(string.encodeToByteArray())
  inline fun <reified T> writeAsJsonNow(someData: T) {
	writeStringNow(Json.encodeToString(someData))
  }
}