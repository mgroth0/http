package matt.http.headers

import matt.http.HTTPDslMarker
import matt.http.headers.AuthType.Basic
import matt.http.req.MutableHTTPRequest
import matt.http.req.valueForHeader
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.model.op.convert.StringConverter
import matt.model.op.convert.StringStringConverter
import matt.prim.base64.encodeToBase64
import matt.prim.str.joinWithSpaces

fun MutableHTTPRequest.headers(op: HTTPHeaders.() -> Unit) {
    HTTPHeaders(this).apply(op)
}

@HTTPDslMarker
class HTTPHeaders internal constructor(private val con: MutableHTTPRequest) {


    var contentType: HTTPMediaType? by propProvider("Content-Type", HTTPContentTypeConverter)
    var accept: HTTPMediaType? by propProvider("Accept", HTTPContentTypeConverter)
    var auth: AuthHeader? by propProvider("Authorization", BearerConverter)
    fun setMySpecialBearerAuth(name: String, token: AuthHeader) {
        addHeader("Authorization-$name", token, BearerConverter)
    }


    private fun <T> validateHeaderIsAbsentOrHasValue(header: String, value: T, converter: StringConverter<T>): T? {
        val oldValue = con.valueForHeader(header)
        val oldValueConverted = oldValue?.let { converter.fromString(it) }
        require(oldValue == null || oldValueConverted == value) {
            "unclear if I am adding or setting here (oldValue=$oldValue,newValue=$value)"
        }
        return oldValueConverted
    }

    private fun <T> addHeader(key: String, value: T, converter: StringConverter<T>) {
        val oldValueConverted = validateHeaderIsAbsentOrHasValue(key, value, converter)
        if (oldValueConverted != value) {
            con.addHeader(key, converter.toString(value))
        }
    }

    private fun addHeader(key: String, value: String) = addHeader(key, value, StringStringConverter)

    /*only commenting this out because it is unused and I'm dealing with JDK 1.8 inline issues*/
    /*private fun propProvider(key: String) = provider {
      varProp(
        getter = { con.valueForHeader(key) },
        setter = {
          require(it != null) {
            "not sure how to handle this yet"
          }
          addHeader(key, it)
        }
      )
    }*/

    private fun <T> propProvider(key: String, converter: StringConverter<T & Any>) = provider {
        varProp(
            getter = {
                val s = con.valueForHeader(key)
                s?.let { converter.fromString(s) }
            },
            setter = {
                require(it != null) {
                    "not sure how to handle this yet"
                }
                addHeader(key, it, converter)
            }
        )
    }

    @Suppress("UNUSED_PARAMETER")
    operator fun set(s: String, value: String) {
        error("unclear how to set or if I should set now that I understand that its not a map")
    }
}


enum class HTTPMediaType(val string: String? = null) {
    applicationJson("application/json"),
    applicationJsonCharsetUTF8("application/json;charset=UTF-8"),
    applicationVndHerokuJson("application/vnd.heroku+json; version=3"),
    applicationVndGitHubJson("application/vnd.github+json");

    fun asString() = string ?: name
}

object HTTPContentTypeConverter : StringConverter<HTTPMediaType> {
    override fun toString(t: HTTPMediaType): String {
        return t.asString()
    }

    override fun fromString(s: String): HTTPMediaType {
        return HTTPMediaType.values().firstOrNull {
            it.asString() == s
        } ?: error("could not find content type \"$s\"")
    }

}


object BearerConverter : StringConverter<AuthHeader> {
    override fun toString(t: AuthHeader): String {
        return arrayOf(t.authType.name, t.token).joinWithSpaces()
    }

    override fun fromString(s: String): AuthHeader {
        return AuthHeader(
            authType = AuthType.valueOf(s.substringBefore(' ').trim()),
            token = s.substringAfter(' ').trim()
        )
    }

}


enum class AuthType {
    Bearer, Token, Basic
}

data class AuthHeader(
    val authType: AuthType,
    val token: String
)

fun basicAuth(username: String, password: String) = AuthHeader(Basic, "$username:$password".encodeToBase64()).also {
//    println("Created basic auth header")
//    println("\tUSERNAME=$username")
//    println("\tPW=$password")
//    println("\t${BearerConverter.toString(it)}")
}