package matt.http.headers.content

import matt.lang.charset.DEFAULT_CHARSET_NAME_CAP
import matt.prim.converters.StringConverter


enum class HTTPMediaType(val string: String? = null) {
    applicationJson("application/json"),
    applicationJsonCharsetUTF8("application/json;charset=$DEFAULT_CHARSET_NAME_CAP"),
    applicationVndHerokuJson("application/vnd.heroku+json; version=3"),
    applicationVndGitHubJson("application/vnd.github+json"),
    textPlain("text/plain");

    fun asString() = string ?: name
}

object HTTPContentTypeConverter : StringConverter<HTTPMediaType> {
    override fun toString(t: HTTPMediaType): String = t.asString()

    override fun fromString(s: String): HTTPMediaType = HTTPMediaType.entries.firstOrNull {
        it.asString() == s
    } ?: error("could not find content type \"$s\"")

}


