package matt.http.req.requester

import kotlinx.coroutines.delay
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPConnection
import matt.http.connection.HTTPConnectionProblem
import matt.http.lib.HTTPRequestBuilder
import matt.http.req.HTTPRequest
import matt.http.req.requester.problems.ClientErrorException
import matt.http.req.requester.problems.RedirectionException
import matt.http.req.requester.problems.ServerErrorException
import matt.http.req.requester.problems.TooManyRetrysException
import matt.http.req.requester.problems.UnauthorizedException
import matt.http.req.requester.problems.WeirdStatusCodeException
import matt.lang.anno.SeeURL
import matt.model.code.delegate.LimitedInt
import matt.time.dur.isNotZero
import kotlin.time.Duration

class HTTPRequester {

  companion object {
	val DEFAULT by lazy {
	  HTTPRequester()
	}
  }

  var request: HTTPRequest = HTTPRequest.EXAMPLE
  var timeout: Duration? = null
  var numAttempts by LimitedInt(1, min = 1)

  var interAttemptWait = Duration.ZERO

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


  var retryOn: suspend (HTTPConnectResult)->Boolean = { false }
  var verbose = false


  suspend fun send(): HTTPConnectResult {
	for (attemptNum in 0 until numAttempts) {
	  val attempt = sendFromLib(timeout)
	  if (retryOn(attempt)) Unit/*do nothing*/
	  else {
		return checkConnection(attempt)
	  }
	  if (attemptNum < (numAttempts - 1) && interAttemptWait.isNotZero) delay(interAttemptWait)
	}
	return TooManyRetrysException(numAttempts)
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
