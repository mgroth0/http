package matt.http.headers

import matt.http.HTTPDslMarker
import matt.http.req.HTTPRequest
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.model.op.convert.StringConverter
import matt.prim.str.joinWithSpaces

fun HTTPRequest.headers(op: HTTPHeaders.()->Unit) {
  HTTPHeaders(this).apply(op)
}

@HTTPDslMarker
class HTTPHeaders internal constructor(private val con: HTTPRequest) {


  var contentType: HTTPMediaType? by propProvider("Content-Type", HTTPContentTypeConverter)
  var accept: HTTPMediaType? by propProvider("Accept", HTTPContentTypeConverter)
  var auth: Auth? by propProvider("Authorization", BearerConverter)

  private fun propProvider(key: String) = provider {
	varProp(
	  getter = {
		con.getRequestProperty(key)
	  },
	  setter = {
		con.setRequestProperty(key, it)
	  }
	)
  }

  private fun <T> propProvider(key: String, converter: StringConverter<T & Any>) = provider {


	varProp(
	  getter = {
		val s = con.getRequestProperty(key)
		s?.let { converter.fromString(s) }
	  },
	  setter = {
		con.setRequestProperty(key, it?.let { itNonNull ->
		  converter.toString(itNonNull)
		})
	  }
	)
  }

  operator fun set(s: String, value: String) {
	con.setRequestProperty(s, value)
  }
}


enum class HTTPMediaType(val string: String? = null) {
  applicationJson("application/json"),
  applicationJsonCharsetUTF8("application/json;charset=UTF-8"),
  applicationVndHerokuJson("application/vnd.heroku+json; version=3"),
  applicationVndGitHubJson("application/vnd.github+json");

  fun asString() = string ?: name
}

object HTTPContentTypeConverter: StringConverter<HTTPMediaType> {
  override fun toString(t: HTTPMediaType): String {
	return t.asString()
  }

  override fun fromString(s: String): HTTPMediaType {
	return HTTPMediaType.values().firstOrNull {
	  it.asString() == s
	} ?: error("could not find content type \"$s\"")
  }

}





enum class AuthType {
  Bearer, Token
}

data class Auth(
  val authType: AuthType,
  val token: String
)


object BearerConverter: StringConverter<Auth> {
  override fun toString(t: Auth): String {
	return arrayOf(t.authType.name, t.token).joinWithSpaces()
  }

  override fun fromString(s: String): Auth {
	return Auth(authType = AuthType.valueOf(s.substringBefore(' ').trim()), token = s.substringAfter(' ').trim())
  }

}

