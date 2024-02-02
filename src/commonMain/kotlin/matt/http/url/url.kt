
package matt.http.url

import matt.http.url.valid.isValidHttpUrl
import matt.lang.model.file.AnyResolvableFileOrUrl
import matt.lang.model.file.FileOrUrlResolver
import matt.lang.model.file.ResolvableFileOrUrl
import matt.lang.model.file.UnsafeFilePath

interface CommonUrl<U: CommonUrl<U>> : ResolvableFileOrUrl<U> {

    companion object {
        const val URL_SEP = "/"
    }
}



const val URL_SEP = "/"


interface UrlResolver<U: UrlResolver<U>>: FileOrUrlResolver<U> {
    override fun resolve(other: String): U
    override fun get(item: String): U
    override fun plus(other: String): U

}

expect class MURL(path: String) : CommonUrl<MURL>, UrlResolver<MURL> {

    val protocol: String

    override val path: String

    override fun resolve(other: String): MURL

    override fun toString(): String

    suspend fun loadText(): String


    override operator fun plus(other: String): MURL

    override operator fun get(item: String): MURL

}


val EXAMPLE_MURL by lazy {
    MURL("https://example.com/")
}

fun fileOrURL(s: String): AnyResolvableFileOrUrl = if (s.isValidHttpUrl()) MURL(s) else UnsafeFilePath(s)
