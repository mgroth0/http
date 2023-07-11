package matt.http.url

import matt.file.FileOrURL
import matt.file.construct.mFile
import matt.http.url.valid.isValidHttpUrl

interface CommonURL : FileOrURL {

    override val cpath: String

    companion object {
        const val URL_SEP = "/"
    }
}



const val URL_SEP = "/"

expect class MURL(path: String) : CommonURL {

    val protocol: String


    override fun resolve(other: String): MURL

    override fun toString(): String

    suspend fun loadText(): String


}


val EXAMPLE_MURL by lazy {
    MURL("https://example.com/")
}

fun fileOrURL(s: String): FileOrURL {
    return if (s.isValidHttpUrl()) MURL(s) else mFile(s)
}