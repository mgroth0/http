
package matt.http.url

import matt.http.url.common.CommonUrl
import matt.http.url.common.UrlResolver


expect class MURL(path: String) : CommonUrl<MURL>, UrlResolver<MURL> {

    val protocol: String

    override val path: String

    override fun resolve(other: String): MURL

    override fun toString(): String

    suspend fun loadText(): String


    override operator fun plus(other: String): MURL

    override operator fun get(item: String): MURL
}


