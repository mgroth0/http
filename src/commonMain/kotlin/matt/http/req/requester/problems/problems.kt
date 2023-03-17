package matt.http.req.requester.problems

import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPConnectionProblem
import matt.prim.str.joinWithNewLines
import matt.prim.str.maybePlural
import kotlin.time.Duration


class HTTPRequestAttempt(
  val tSent: Duration,
  val tGotResult: Duration,
  val result: HTTPConnectResult
)

sealed class TooMuchRetryingException(attempts: List<HTTPRequestAttempt>):
	HTTPConnectionProblem(
	  "No successful connection after ${attempts.size} ${
		maybePlural(
		  attempts.size, "attempt"
		)
	  }\n${attempts.joinWithNewLines { "\t${it.tSent}-${it.tGotResult}\t${it.result}" }}"
	)

class TooManyRetrysException(attempts: List<HTTPRequestAttempt>): TooMuchRetryingException(
  attempts
)

class TriedForTooLongException(attempts: List<HTTPRequestAttempt>): TooMuchRetryingException(
  attempts
)

sealed class NoConnectionException(message: String): HTTPConnectionProblem("No Connection: $message")
class HTTPExceptionWhileCreatingConnection(cause: Exception): NoConnectionException("Exception while creating connection: ${cause}: ${cause.message}")
class HTTPTimeoutException(duration: Duration): NoConnectionException("Timeout after $duration")
sealed class HTTPBadConnectionException(status: Short, message: String): HTTPConnectionProblem("$status: $message")
class WeirdStatusCodeException(status: Short, message: String): HTTPBadConnectionException(
  status, "is a weird status code. text=${message}"
)

class RedirectionException(status: Short, message: String): HTTPBadConnectionException(status, message)
sealed class HTTPErrorException(status: Short, message: String): HTTPBadConnectionException(status, message)
open class ClientErrorException(status: Short, message: String): HTTPErrorException(status, message)
class NotFoundException(url: String): ClientErrorException(404, "Not Found: $url")
class UnauthorizedException(message: String): ClientErrorException(401, message)
open class ServerErrorException(status: Short, message: String): HTTPErrorException(status, message)
class ServiceUnavailableException(message: String): ServerErrorException(401, message)