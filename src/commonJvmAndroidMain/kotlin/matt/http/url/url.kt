@file:JvmName("UrlJvmAndroidKt")

package matt.http.url

import matt.http.http
import matt.lang.model.file.MacFileSystem
import matt.lang.model.file.ensureSuffix
import matt.model.url.URLLike
import java.net.URI
import java.net.URL


actual class MURL actual constructor(actual override val path: String) : CommonUrl<MURL>, URLLike, UrlResolver<MURL> {

    constructor(uri: URI) : this(uri.toString())
    constructor(url: URL) : this(url.toString())

    override fun toJavaURI(): URI {
        return URI(path)
    }

    val jURL: URL by lazy { toJavaURI().toURL() }

    actual val protocol: String by lazy { jURL.protocol }

    actual override operator fun get(item: String): MURL = resolve(item)
    actual override fun resolve(other: String): MURL {
        /*the java way is weird and discards a segment*/
        return MURL(path.ensureSuffix(MacFileSystem.separator) + other.removePrefix(MacFileSystem.separator))
        /*return MURL(jURL.toURI().resolve(other).toString())*/
    }

    override fun toJavaURL() = jURL

    actual override fun toString() = path

    actual suspend fun loadText() = http().text()

    fun open(): Nothing = TODO()

    actual override operator fun plus(other: String): MURL = resolve(other)

}


