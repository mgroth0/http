package matt.http.headers

import matt.http.HTTPDslMarker
import matt.http.headers.auth.AuthHeader
import matt.http.headers.base.HTTPHeadersBase
import matt.http.headers.content.HTTPMediaType
import matt.http.headers.key.Accept
import matt.http.headers.key.Auth
import matt.http.headers.key.ContentType
import matt.http.headers.key.MattMd5
import matt.http.headers.key.MySpecialBearerAuth
import matt.http.headers.key.XGitHubApiVersion
import matt.http.req.MutableHeaders
import matt.model.data.hash.md5.MD5

fun MutableHeaders.headers(op: HTTPHeaders.() -> Unit) {
    HTTPHeaders(this).apply(op)
}

@HTTPDslMarker
class HTTPHeaders internal constructor(con: MutableHeaders) : HTTPHeadersBase(con) {


    var contentType: HTTPMediaType? by propProvider(ContentType)
    var accept: HTTPMediaType? by propProvider(Accept)
    var auth: AuthHeader? by propProvider(Auth)
    var md5: MD5? by propProvider(MattMd5)
    fun setMySpecialBearerAuth(
        name: String,
        token: AuthHeader
    ) {
        setHeader(MySpecialBearerAuth(name), token)
    }

    var githubApiVersion: String? by propProvider(XGitHubApiVersion)
}


