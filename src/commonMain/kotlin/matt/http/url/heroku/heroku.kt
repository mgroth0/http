package matt.http.url.heroku

import kotlinx.serialization.Serializable
import matt.http.api.AuthenticatedApi
import matt.http.headers.auth.AuthHeader
import matt.http.url.MURL
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class HerokuHost(val url: String)

class AnApiForMyHerokuApp(
    url: HerokuHost,
    auths: Map<String, AuthHeader> = mapOf()
) : AuthenticatedApi(auths = auths) {
    override val urlPrefix = MURL(url.url)/*herokuAbsoluteHostName(absoluteAppName)*/

    override fun toString(): String = "API for Heroku App: $urlPrefix"
}
