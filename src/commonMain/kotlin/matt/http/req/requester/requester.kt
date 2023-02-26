package matt.http.req.requester

import kotlinx.coroutines.delay
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPConnection
import matt.http.connection.HTTPConnectionProblem
import matt.http.lib.HTTPRequestBuilder
import matt.http.req.HTTPRequest
import matt.http.req.ImmutableHTTPRequest
import matt.http.req.requester.problems.ClientErrorException
import matt.http.req.requester.problems.HTTPRequestAttempt
import matt.http.req.requester.problems.RedirectionException
import matt.http.req.requester.problems.ServerErrorException
import matt.http.req.requester.problems.TooManyRetrysException
import matt.http.req.requester.problems.TriedForTooLongException
import matt.http.req.requester.problems.UnauthorizedException
import matt.http.req.requester.problems.WeirdStatusCodeException
import matt.lang.anno.SeeURL
import matt.time.UnixTime
import matt.time.dur.isNotZero
import kotlin.time.Duration

data class HTTPRequester(
  val request: ImmutableHTTPRequest = HTTPRequest.EXAMPLE,
  val timeout: Duration? = null,
  val numAttempts: Int = 1,
  val keepTryingFor: Duration = Duration.ZERO,
  val interAttemptWait: Duration = Duration.ZERO,
  val retryOn: suspend (HTTPConnectResult)->Boolean = { false },
  val verbose: Boolean = false
) {

  init {
	require(numAttempts >= 1)
  }

  companion object {
	val DEFAULT by lazy {
	  HTTPRequester()
	}
  }


  @Suppress("SetterBackingFieldAssignment") @SeeURL("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status")
  var checkConnection: suspend (HTTPConnectResult)->HTTPConnectResult = { it ->
	when (it) {
	  is HTTPConnection -> {
		when (val statusCode = it.statusCode().value.toShort()) {
		  in 300..399  -> RedirectionException(statusCode, it.text())
		  in 400..499  -> when (statusCode.toInt()) {
			401  -> UnauthorizedException(it.text())
			else -> ClientErrorException(statusCode, it.text())
		  }

		  in 500..599  -> ServerErrorException(statusCode, it.text())
		  !in 100..300 -> WeirdStatusCodeException(statusCode, it.text())
		  else         -> it
		}
	  }

	  else              -> it
	}
  }
	set(_) {
	  error("not ready to change this. I would want to modify it in a smart way, not change it with brute force.")
	}


  suspend fun send(): HTTPConnectResult {
	val startedTrying = UnixTime()
	var triedFor: Duration = Duration.ZERO
	val attempts = mutableListOf<HTTPRequestAttempt>()
	for (attemptNum in 0 until numAttempts) {
	  val tSent = UnixTime() - startedTrying
	  val attempt = sendFromLib(timeout)
	  val tGotResult = UnixTime() - startedTrying
	  attempts += HTTPRequestAttempt(tSent = tSent, tGotResult = tGotResult, result = attempt)
	  if (retryOn(attempt)) Unit/*do nothing*/
	  else {
		return checkConnection(attempt)
	  }
	  if (
		attemptNum < (numAttempts - 1)
		&& interAttemptWait.isNotZero
	  ) {
		delay(interAttemptWait)
	  }
	  triedFor = UnixTime() - startedTrying
	  if (triedFor > keepTryingFor) {
		return TriedForTooLongException(attempts)
	  }
	}
	return TooManyRetrysException(attempts)
  }

  suspend fun sendAndThrowUnlessConnectedCorrectly(): HTTPConnection {
	return when (val result = send()) {
	  is HTTPConnectionProblem -> throw result
	  is HTTPConnection        -> result
	}
  }

  private suspend fun sendFromLib(timeout: Duration? = null) = HTTPRequestBuilder().apply {
	initialize(
	  url = request.url, method = request.method, bodyWriter = request.bodyWriter
	)
	applyHeaders(request.headersSnapshot())
	applyTimeout(timeout)
  }.send()
}
