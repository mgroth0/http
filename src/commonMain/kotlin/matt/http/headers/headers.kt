package matt.http.headers

import matt.http.HTTPDslMarker
import matt.http.headers.auth.AuthHeader
import matt.http.headers.auth.BearerConverter
import matt.http.headers.base.HTTPHeadersBase
import matt.http.headers.content.HTTPContentTypeConverter
import matt.http.headers.content.HTTPMediaType
import matt.http.req.MutableHeaders
import matt.model.data.hash.md5.MD5
import matt.model.data.hash.md5.MD5Converter
import matt.model.op.convert.StringStringConverter

fun MutableHeaders.headers(op: HTTPHeaders.() -> Unit) {
    HTTPHeaders(this).apply(op)
}

@HTTPDslMarker
class HTTPHeaders internal constructor(con: MutableHeaders) : HTTPHeadersBase(con) {


    var contentType: HTTPMediaType? by propProvider("Content-Type", HTTPContentTypeConverter)
    var accept: HTTPMediaType? by propProvider("Accept", HTTPContentTypeConverter)
    var auth: AuthHeader? by propProvider("Authorization", BearerConverter)
    var md5: MD5? by propProvider("matt-md5", MD5Converter)
    fun setMySpecialBearerAuth(
        name: String,
        token: AuthHeader
    ) {
        addHeader("Authorization-$name", token, BearerConverter)
    }

    var githubApiVersion: String? by propProvider("X-GitHub-Api-Version", StringStringConverter)

}


