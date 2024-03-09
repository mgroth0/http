
package matt.http.j

import matt.http.http
import matt.http.req.MutableHTTPRequest
import matt.http.req.requester.HTTPRequester
import matt.http.url.MURL
import java.net.URI
import java.net.URL


suspend fun http(
    url: URI,
    requester: HTTPRequester = HTTPRequester.DEFAULT,
    op: MutableHTTPRequest.() -> Unit = {}
) = MURL(url).http(op = op, requester = requester)

suspend fun http(
    url: URL,
    requester: HTTPRequester = HTTPRequester.DEFAULT,
    op: MutableHTTPRequest.() -> Unit = {}
) = MURL(url).http(requester = requester, op)


