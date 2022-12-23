package matt.http.url

import matt.file.FileOrURL
import matt.file.construct.mFile



interface CommonURL: FileOrURL {

  override val cpath: String

  companion object {
	const val URL_SEP = "/"
  }
}

expect class MURL(path: String): CommonURL {


  val protocol: String

  override fun resolve(other: String): MURL

  override fun toString(): String

  fun loadText(): String
}
val EXAMPLE_MURL by lazy {
  MURL("https://example.com/")
}

fun String.isValidHttpUrl(): Boolean {
  val url = try {
	MURL(this)
  } catch (e: Exception) {
	return false
  }

  return url.protocol === "http:" || url.protocol === "https:";
}

fun fileOrURL(s: String): FileOrURL {
  return if (s.isValidHttpUrl()) MURL(s) else mFile(s)
}


