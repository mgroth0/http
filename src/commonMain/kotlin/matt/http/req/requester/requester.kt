package matt.http.req.requester

import matt.http.connection.HTTPConnection
import matt.http.connection.HTTPConnectionProblem
import matt.http.req.HTTPRequest
import matt.http.req.ImmutableHTTPRequest
import matt.http.req.requester.intercept.StatusCodeChecker
import matt.http.req.requester.intercept.outer.RetryingHttpInterceptor
import matt.http.req.requester.intercept.outer.RetryingHttpInterceptor.Companion.NO_RETRIES
import kotlin.time.Duration


data class HTTPRequester(
    val request: ImmutableHTTPRequest = HTTPRequest.EXAMPLE,
    val timeout: Duration = Duration.INFINITE,
    val retryer: RetryingHttpInterceptor = NO_RETRIES,
    val suppressAnyErrorReport: Boolean = false
) {

    companion object {
        val DEFAULT by lazy {
            HTTPRequester()
        }
    }


    suspend fun sendAndThrowUnlessConnectedCorrectly(): HTTPConnection {
        val result =
            retryer.intercept(
                request = request,
                timeout = timeout,
                onResult = {
                    mainInterceptor.intercept(request, it)
                }
            )
        when (result) {
            is HTTPConnectionProblem -> throw result
            else                     -> Unit
        }
        return result as HTTPConnection
    }

    private val mainInterceptor = StatusCodeChecker(suppressAnyErrorReport = suppressAnyErrorReport)
}


