package matt.http.headers

import matt.http.HTTPDslMarker
import matt.http.req.MutableHTTPRequest
import matt.http.req.valueForHeader
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.model.op.convert.StringConverter
import matt.prim.str.joinWithSpaces

fun MutableHTTPRequest.headers(op: HTTPHeaders.()->Unit) {
  HTTPHeaders(this).apply(op)
}

@HTTPDslMarker
class HTTPHeaders internal constructor(private val con: MutableHTTPRequest) {


  var contentType: HTTPMediaType? by propProvider("Content-Type", HTTPContentTypeConverter)
  var accept: HTTPMediaType? by propProvider("Accept", HTTPContentTypeConverter)
  var auth: AuthHeader? by propProvider("Authorization", BearerConverter)

  private fun propProvider(key: String) = provider {
	varProp(
	  getter = { con.valueForHeader(key) },
	  setter = {
		val oldValue = con.valueForHeader(key)
		require(oldValue == null || oldValue == it) {
		  "unclear if I am adding or setting here (oldValue=$oldValue,newValue=$it)"
		}
		require(it != null) {
		  "not sure how to handle this yet"
		}
		if (oldValue != it) {
		  con.addHeader(key, it)
		}
	  }
	)
  }

  private fun <T> propProvider(key: String, converter: StringConverter<T & Any>) = provider {
	varProp(
	  getter = {
		val s = con.valueForHeader(key)
		s?.let { converter.fromString(s) }
	  },
	  setter = {
		val oldValue = con.valueForHeader(key)
		val oldValueConverted = oldValue?.let { converter.fromString(it) }
		require(oldValue == null || oldValueConverted == it) {
		  "unclear if I am adding or setting here (oldValue=$oldValue,newValue=$it)"
		}
		require(it != null) {
		  "not sure how to handle this yet"
		}
		if (oldValueConverted != it) {
		  con.addHeader(key, it.let { itNonNull ->
			converter.toString(itNonNull)
		  })
		}
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





object BearerConverter: StringConverter<AuthHeader> {
  override fun toString(t: AuthHeader): String {
	return arrayOf(t.authType.name, t.token).joinWithSpaces()
  }

  override fun fromString(s: String): AuthHeader {
	return AuthHeader(authType = AuthType.valueOf(s.substringBefore(' ').trim()), token = s.substringAfter(' ').trim())
  }

}



enum class AuthType {
  Bearer, Token
}

data class AuthHeader(
  val authType: AuthType,
  val token: String
)