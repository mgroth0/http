package matt.http.headers

import matt.http.HTTPDslMarker
import matt.http.req.HTTPRequest
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.model.op.convert.StringConverter
import matt.prim.str.joinWithSpaces


@HTTPDslMarker
class HTTPHeaders internal constructor(private val con: HTTPRequest) {


  var contentType: HTTPContentType? by propProvider("Content-Type", HTTPContentTypeConverter)
  var accept: HTTPSpecificContentType? by propProvider("Accept", HTTPSpecificContentTypeConverter)
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


enum class HTTPContentType(val string: String? = null) {
  applicationJson("application/json");

  fun asString() = string ?: name
}

object HTTPContentTypeConverter: StringConverter<HTTPContentType> {
  override fun toString(t: HTTPContentType): String {
	return t.asString()
  }

  override fun fromString(s: String): HTTPContentType {
	return HTTPContentType.values().firstOrNull {
	  it.asString() == s
	} ?: error("could not find content type \"$s\"")
  }

}

enum class HTTPSpecificContentType(val string: String? = null) {
  applicationVndHerokuJson("application/vnd.heroku+json; version=3"),
  applicationVndGitHubJson("application/vnd.github+json");

  fun asString() = string ?: name
}

object HTTPSpecificContentTypeConverter: StringConverter<HTTPSpecificContentType> {
  override fun toString(t: HTTPSpecificContentType): String {
	return t.asString()
  }

  override fun fromString(s: String): HTTPSpecificContentType {
	return HTTPSpecificContentType.values().firstOrNull {
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