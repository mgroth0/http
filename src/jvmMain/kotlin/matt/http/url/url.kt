package matt.http.url

import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers


actual class MURL actual constructor(path: String): CommonURL {

  override val cpath = path

  val jURL: URL = URI(path).toURL()

  actual val protocol: String = jURL.protocol

  actual override fun resolve(other: String): MURL {
	return MURL(jURL.toURI().resolve(other).toString())
  }

  actual override fun toString() = cpath

  actual fun loadText(): String {

    /*this doesnt give detailed failure objects with error codes and stuff, which are necessary!*/
    /*jURL.readText()*/

    val req = HttpRequest.newBuilder().GET().uri(jURL.toURI()).build()
    val response = HttpClient.newHttpClient().send(req,BodyHandlers.ofString())
    return response.body()

  }

  fun open() = Desktop.getDesktop().browse(jURL.toURI())

}

