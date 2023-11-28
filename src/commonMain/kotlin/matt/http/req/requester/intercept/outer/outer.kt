package matt.http.req.requester.intercept.outer

import kotlinx.coroutines.delay
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPConnectionProblem
import matt.http.connection.SingleHTTPConnectResult
import matt.http.lib.MyNewHTTPRequestBuilder
import matt.http.req.ImmutableHTTPRequest
import matt.http.req.requester.problems.HTTPRequestAttempt
import matt.http.req.requester.problems.TooManyRetrysException
import matt.http.req.requester.problems.TriedForTooLongException
import matt.lang.assertions.require.requirePositive
import matt.time.UnixTime
import matt.time.dur.isNotZero
import kotlin.time.Duration


interface HttpOutermostInterceptor {
    suspend fun intercept(
        request: ImmutableHTTPRequest,
        timeout: Duration,
        onResult: suspend (SingleHTTPConnectResult) -> SingleHTTPConnectResult
    ): HTTPConnectResult
}

data class RetryingHttpInterceptor(
    val numAttempts: Int,
    val keepTryingFor: Duration = Duration.ZERO,
    val interAttemptWait: Duration = Duration.ZERO,
    val retryOn: (SingleHTTPConnectResult) -> Boolean = { false }
) : HttpOutermostInterceptor {

    companion object {
        val NO_RETRIES = RetryingHttpInterceptor(1)
    }


    init {
        requirePositive(numAttempts) {
            "numAttempts should be positive not $numAttempts"
        }
    }


    override suspend fun intercept(
        request: ImmutableHTTPRequest,
        timeout: Duration,
        onResult: suspend (SingleHTTPConnectResult) -> SingleHTTPConnectResult
    ): HTTPConnectResult {
        val startedTrying = UnixTime()
        val attempts = mutableListOf<HTTPRequestAttempt>()
        val problems = mutableListOf<Exception>()
        for (attemptNum in 0 until numAttempts) {
            val tSent = UnixTime() - startedTrying
            val attempt = InnerHttpEngine(timeout = timeout).sendNewRequest(
                request = request, timeout = timeout
            )
            val tGotResult = UnixTime() - startedTrying
            attempts += HTTPRequestAttempt(tSent = tSent, tGotResult = tGotResult, result = attempt)
            when (val r = onResult(attempt)) {
                is HTTPConnectionProblem -> {
                    if (retryOn(r)) {
                        problems += r
                    } else {
                        throw r
                    }
                }

                else                     -> return r

            }
            if (attemptNum < (numAttempts - 1) && interAttemptWait.isNotZero) {
                delay(interAttemptWait)
            }
            if (tGotResult > keepTryingFor) {
                return TriedForTooLongException(uri = request.url, attempts, problems)
            }
        }
        return TooManyRetrysException(uri = request.url, attempts, problems)
    }
}

class InnerHttpEngine(val timeout: Duration) {

    suspend fun sendNewRequest(
        request: ImmutableHTTPRequest,
        timeout: Duration
    ): SingleHTTPConnectResult {
        val reqBuilder = MyNewHTTPRequestBuilder()
        reqBuilder.initialize(
            url = request.url, method = request.method, bodyWriter = request.bodyWriter
        )
        reqBuilder.applyHeaders(request.headersSnapshot())
        reqBuilder.applyTimeout(timeout)
        return reqBuilder.send()
    }

}