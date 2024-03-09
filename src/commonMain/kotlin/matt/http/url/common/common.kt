package matt.http.url.common

import matt.http.url.MURL
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

val EXAMPLE_MURL by lazy {
    MURL("https://example.com/")
}

fun fileOrURL(s: String): AnyResolvableFileOrUrl = if (s.isValidHttpUrl()) MURL(s) else UnsafeFilePath(s)
