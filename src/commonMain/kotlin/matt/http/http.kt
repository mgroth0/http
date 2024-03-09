package matt.http

import matt.http.connection.HTTPConnection
import matt.http.req.MutableHTTPRequest
import matt.http.req.requester.HTTPRequester
import matt.http.url.MURL
import matt.lang.model.file.AnyResolvableFileOrUrl
import kotlin.jvm.JvmName


@DslMarker
annotation class HTTPDslMarker

@JvmName("http1")
suspend fun http(
    url: AnyResolvableFileOrUrl,
    requester: HTTPRequester = HTTPRequester.DEFAULT,
    op: MutableHTTPRequest.() -> Unit = {}
) = url.http(op = op, requester = requester)

suspend fun http(
    url: String,
    requester: HTTPRequester = HTTPRequester.DEFAULT,
    op: MutableHTTPRequest.() -> Unit = {}
) = MURL(url).http(op = op, requester = requester)

suspend fun AnyResolvableFileOrUrl.http(
    requester: HTTPRequester = HTTPRequester.DEFAULT,
    op: MutableHTTPRequest.() -> Unit = {}
): HTTPConnection {


    val req = MutableHTTPRequest()
    req.url = path
    req.op()
    val snap = req.snapshot()
    return requester.copy(request = snap).sendAndThrowUnlessConnectedCorrectly()
}



/*fail fast instead of hanging!*/
const val FAIL_SELENIUM_ID = "FAIL_SELENIUM_ID"
