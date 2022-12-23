package matt.http.url

import matt.file.URLLike
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers



actual class MURL actual constructor(path: String): CommonURL, URLLike {



  constructor(uri: URI): this(uri.toString())
  constructor(url: URL): this(url.toString())

  override val cpath = path

  override fun toJavaURI(): URI {
    return URI(cpath)
  }

  val jURL: URL = toJavaURI().toURL()

  actual val protocol: String = jURL.protocol

  operator fun get(other: String) = resolve(other)
  actual override fun resolve(other: String): MURL {
    /*the java way is weird and discards a segment*/
    return MURL(cpath.removeSuffix("/") + "/" + other.removePrefix("/"))
	/*return MURL(jURL.toURI().resolve(other).toString())*/
  }

  override fun toJavaURL() = jURL

  actual override fun toString() = cpath

  actual fun loadText(): String {

    /*this doesnt give detailed failure objects with error codes and stuff, which are necessary!*/
    /*jURL.readText()*/

    val req = HttpRequest.newBuilder().GET().uri(jURL.toURI()).build()
    val response = HttpClient.newHttpClient().send(req,BodyHandlers.ofString())
    return response.body()

  }

  fun open() = Desktop.getDesktop().browse(jURL.toURI())

  override operator fun plus(other: String): MURL = resolve(other)

}

