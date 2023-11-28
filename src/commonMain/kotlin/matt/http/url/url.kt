
package matt.http.url

import matt.http.url.valid.isValidHttpUrl
import matt.lang.model.file.FileOrURL
import matt.lang.model.file.UnsafeFilePath

interface CommonURL : FileOrURL {

    override val cpath: String

    companion object {
        const val URL_SEP = "/"
    }
}



const val URL_SEP = "/"

expect class MURL(path: String) : CommonURL {

    val protocol: String

    override val cpath: String

    override fun resolve(other: String): MURL

    override fun toString(): String

    suspend fun loadText(): String


    override operator fun plus(other: String): MURL

    override operator fun get(item: String): FileOrURL

}


val EXAMPLE_MURL by lazy {
    MURL("https://example.com/")
}

fun fileOrURL(s: String): FileOrURL {
    return if (s.isValidHttpUrl()) MURL(s) else UnsafeFilePath(s)
}