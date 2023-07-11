package matt.http.method

import io.ktor.http.*

fun HttpMethod.toMyHttpMethod() = HTTPMethod.valueOf(this.value)

enum class HTTPMethod { GET, POST, PUT, PATCH, DELETE }