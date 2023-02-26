package matt.http.req.requester.problems

import matt.http.connection.HTTPConnectionProblem
import matt.prim.str.maybePlural
import kotlin.time.Duration


sealed class TooMuchRetryingException(numAttempts: Int, triedFor: Duration): HTTPConnectionProblem(
  "No successful connection after $numAttempts ${
	maybePlural(
	  numAttempts, "attempt"
	)
  } (${triedFor})"
)
class TooManyRetrysException(numAttempts: Int, triedFor: Duration): TooMuchRetryingException(numAttempts,triedFor)
class TriedForTooLongException(numAttempts: Int, triedFor: Duration): TooMuchRetryingException(numAttempts,triedFor)

sealed class NoConnectionException(message: String): HTTPConnectionProblem("No Connection: $message")
class HTTPConnectionRefused: NoConnectionException("Connection Refused")
class HTTPTimeoutException(duration: Duration): NoConnectionException("Timeout after $duration")
sealed class HTTPBadConnectionException(status: Short, message: String): HTTPConnectionProblem("$status: $message")
class WeirdStatusCodeException(status: Short, message: String): HTTPBadConnectionException(
  status, "is a weird status code. text=${message}"
)

class RedirectionException(status: Short, message: String): HTTPBadConnectionException(status, message)
sealed class HTTPErrorException(status: Short, message: String): HTTPBadConnectionException(status, message)
open class ClientErrorException(status: Short, message: String): HTTPErrorException(status, message)
class UnauthorizedException(message: String): ClientErrorException(401, message)
class ServerErrorException(status: Short, message: String): HTTPErrorException(status, message)