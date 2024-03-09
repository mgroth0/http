package matt.http.headers.auth

import matt.http.headers.auth.AuthType.Basic
import matt.lang.common.substringAfterSingular
import matt.lang.common.substringBeforeSingular
import matt.prim.base64.encodeToBase64
import matt.prim.converters.StringConverter
import matt.prim.str.joinWithSpaces


enum class AuthType {
    Bearer, Token, Basic
}

data class AuthHeader(
    val authType: AuthType,
    val token: String
)

fun basicAuth(
    username: String,
    password: String
) = AuthHeader(Basic, "$username:$password".encodeToBase64())


object BearerConverter : StringConverter<AuthHeader> {
    override fun toString(t: AuthHeader): String = arrayOf(t.authType.name, t.token).joinWithSpaces()

    override fun fromString(s: String): AuthHeader =
        AuthHeader(
            authType = AuthType.valueOf(s.substringBeforeSingular(' ').trim()),
            token = s.substringAfterSingular(' ').trim()
        )
}
