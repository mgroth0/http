package matt.http.url

import matt.file.MFile
import matt.file.URLLike
import matt.http.http
import matt.prim.str.ensureSuffix
import java.net.URI
import java.net.URL

actual class MURL actual constructor(path: String) : CommonURL, URLLike {

    constructor(uri: URI) : this(uri.toString())
    constructor(url: URL) : this(url.toString())

    override val cpath = path

    override fun toJavaURI(): URI {
        return URI(cpath)
    }

    val jURL: URL by lazy { toJavaURI().toURL() }

    actual val protocol: String by lazy { jURL.protocol }

    override operator fun get(item: String) = resolve(item)
    actual override fun resolve(other: String): MURL {
        /*the java way is weird and discards a segment*/
        return MURL(cpath.ensureSuffix(MFile.unixSeparator) + other.removePrefix(MFile.unixSeparator))
        /*return MURL(jURL.toURI().resolve(other).toString())*/
    }

    override fun toJavaURL() = jURL

    actual override fun toString() = cpath

    actual suspend fun loadText() = http().text()

    fun open(): Nothing = TODO()

    override operator fun plus(other: String): MURL = resolve(other)

}

