package matt.http.url

import matt.file.URLLike
import matt.http.http
import java.awt.Desktop
import java.net.URI
import java.net.URL


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

  actual suspend fun loadText() = http().text()

  fun open() = Desktop.getDesktop().browse(jURL.toURI())

  override operator fun plus(other: String): MURL = resolve(other)

}

