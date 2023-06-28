package matt.http.req.requester

import kotlinx.coroutines.delay
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPConnection
import matt.http.connection.HTTPConnectionProblem
import matt.http.lib.MyHTTPRequestBuilder
import matt.http.report.EndSide.Client
import matt.http.report.HTTPRequestReport
import matt.http.req.HTTPRequest
import matt.http.req.ImmutableHTTPRequest
import matt.http.req.requester.problems.ClientErrorException
import matt.http.req.requester.problems.HTTPRequestAttempt
import matt.http.req.requester.problems.NoConnectionException
import matt.http.req.requester.problems.NotFoundException
import matt.http.req.requester.problems.RedirectionException
import matt.http.req.requester.problems.ServerErrorException
import matt.http.req.requester.problems.ServiceUnavailableException
import matt.http.req.requester.problems.TooManyRetrysException
import matt.http.req.requester.problems.TriedForTooLongException
import matt.http.req.requester.problems.UnauthorizedException
import matt.http.req.requester.problems.UnsupportedMediaType
import matt.http.req.requester.problems.WeirdStatusCodeException
import matt.lang.anno.SeeURL
import matt.model.code.errreport.throwReport
import matt.time.UnixTime
import matt.time.dur.isNotZero
import matt.time.nowKotlinDateTime
import kotlin.ranges.contains
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class HTTPRequester(
    val request: ImmutableHTTPRequest = HTTPRequest.EXAMPLE,
    val timeout: Duration? = null,
    val numAttempts: Int = 1,
    val keepTryingFor: Duration = Duration.ZERO,
    val interAttemptWait: Duration = Duration.ZERO,
    val retryOn: suspend (HTTPConnectResult) -> Boolean = { false },
    val verbose: Boolean = false
) {

    init {
        require(numAttempts >= 1)
    }

    companion object {
        val DEFAULT by lazy {
            HTTPRequester()
        }
        val DEFAULT_RETRYER by lazy {
            DEFAULT.copy(numAttempts = 100, keepTryingFor = 5.seconds, interAttemptWait = 100.milliseconds, retryOn = {
                it is NoConnectionException
            })
        }
    }


    @Suppress("SetterBackingFieldAssignment")
    @SeeURL("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status")
    var checkConnection: suspend (HTTPConnectResult) -> HTTPConnectResult = { it ->
        when (it) {
            is HTTPConnection -> {
                when (val statusCode = it.statusCode().value.toShort()) {
                    in 300..399  -> {
                        RedirectionException(request.url, statusCode, it.text())
                    }

                    in 400..499  -> when (statusCode.toInt()) {
                        401  -> UnauthorizedException(url = request.url, it.text())
                        404  -> NotFoundException(url = request.url)
                        415  -> UnsupportedMediaType(url = request.url, message = it.text())
                        else -> ClientErrorException(uri = request.url, status = statusCode, message = it.text())
                    }

                    in 500..599  -> when (statusCode.toInt()) {
                        503  -> ServiceUnavailableException(uri = request.url, it.text())
                        else -> ServerErrorException(uri = request.url, statusCode, it.text())
                    }

                    !in 100..300 -> WeirdStatusCodeException(uri = request.url, statusCode, it.text())
                    else         -> it
                }
            }

            else              -> it
        }
    }
        set(_) {
            error("not ready to change this. I would want to modify it in a smart way, not change it with brute force.")
        }


    suspend fun send(): Pair<HTTPConnectResult, MyHTTPRequestBuilder> {
        val startedTrying = UnixTime()
        val attempts = mutableListOf<HTTPRequestAttempt>()
        var built: MyHTTPRequestBuilder? = null
        for (attemptNum in 0 until numAttempts) {
            val tSent = UnixTime() - startedTrying
            built = build(timeout)
            val attempt = sendFromLib(built)
            val tGotResult = UnixTime() - startedTrying
            attempts += HTTPRequestAttempt(tSent = tSent, tGotResult = tGotResult, result = attempt)
            if (retryOn(attempt)) Unit/*do nothing*/
            else {
                val checked = checkConnection(attempt)
                if (!retryOn(checked)) return checked to built
            }
            if (attemptNum < (numAttempts - 1) && interAttemptWait.isNotZero) {
                delay(interAttemptWait)
            }
            if (tGotResult > keepTryingFor) {
                return TriedForTooLongException(uri = request.url, attempts) to built
            }
        }
        return TooManyRetrysException(uri = request.url, attempts) to built!!
    }

    suspend fun sendAndThrowUnlessConnectedCorrectly(): HTTPConnection {
        val (result, built) = send()



        return when (result) {
            is HTTPConnectionProblem -> {
                val date = nowKotlinDateTime()
                val report = HTTPRequestReport(
                    side = Client,
                    date = date,
                    uri = request.url,
                    method = request.method.name,
                    headers = request.headers.groupBy { it.first }.mapValues { it.value.map { it.second } },
                    attributes = built.builder.attributes.allKeys,
                    parameters = mapOf() /*not sure if parameters are available from the client side*/,
                    throwReport = throwReport(result)
                )
                report.print()
                throw result
            }

            is HTTPConnection        -> result
        }
    }

    private fun build(timeout: Duration? = null) = MyHTTPRequestBuilder().apply {
        initialize(
            url = request.url, method = request.method, bodyWriter = request.bodyWriter
        )
        applyHeaders(request.headersSnapshot())
        applyTimeout(timeout)
    }

    private suspend fun sendFromLib(built: MyHTTPRequestBuilder) = built.send()
}
