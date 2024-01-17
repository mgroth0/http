package matt.http.url.heroku

import io.ktor.http.headers
import kotlinx.serialization.Serializable
import matt.http.api.APIWithConfiguredHeaders
import matt.http.headers.HTTPHeaders
import matt.http.headers.auth.AuthHeader
import matt.http.url.MURL
import kotlin.jvm.JvmInline


/*class HerokuSite(
    baseAppName: String
) {
    val stagingHost = herokuHostName(baseAppName = baseAppName, staging = true)
    val productionHost = herokuHostName(baseAppName = baseAppName, staging = false)
}

fun herokuHostName(
    baseAppName: String,
    staging: Boolean
) = herokuAbsoluteHostName("${baseAppName}${if (staging) "-staging" else ""}")

fun herokuAbsoluteHostName(
    absoluteAppName: String,
) = MURL("https://$absoluteAppName.herokuapp.com")*/

@Serializable
@JvmInline
value class HerokuHost(val url: String)

class AnApiForMyHerokuApp(
    url: HerokuHost,
    auths: Map<String, AuthHeader> = mapOf()
) : APIWithConfiguredHeaders {
    override val urlPrefix = MURL(url.url)/*herokuAbsoluteHostName(absoluteAppName)*/
    override val defaultHeaders: (HTTPHeaders.() -> Unit) = {
        headers {
            auths.forEach {
                setMySpecialBearerAuth(it.key, it.value)
            }
        }
    }
}
