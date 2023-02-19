package matt.http.headers

import matt.http.HTTPDslMarker
import matt.http.req.HTTPRequest
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.model.op.convert.StringConverter
import matt.prim.str.joinWithSpaces


@HTTPDslMarker
class HTTPHeaders internal constructor(private val con: HTTPRequest) {


  var contentType: String? by propProvider("Content-Type")
  var accept: HTTPContentType? by propProvider<HTTPContentType?>("Accept", HTTPContentTypeConverter)
  var auth: Bearer? by propProvider("Authorization", BearerConverter)

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
  applicationVndGitHubJson("application/vnd.github+json");

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


class Bearer(val token: String)
object BearerConverter: StringConverter<Bearer> {
  private val BEARER = "Bearer"
  override fun toString(t: Bearer): String {
	return arrayOf(BEARER,t.token).joinWithSpaces()
  }

  override fun fromString(s: String): Bearer {
	return Bearer(s.substringAfter(BEARER).trim())
  }

}