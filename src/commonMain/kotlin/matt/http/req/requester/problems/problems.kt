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

sealed class TooMuchRetryingException(
    uri: String,
    attempts: List<HTTPRequestAttempt>
) :
    HTTPConnectionProblem(
        uri = uri,
        "No successful connection after ${attempts.size} ${
            maybePlural(
                attempts.size, "attempt"
            )
        }\n${attempts.joinWithNewLines { "\t${it.tSent}-${it.tGotResult}\t${it.result}" }}"
    )

class TooManyRetrysException(
    uri: String,
    attempts: List<HTTPRequestAttempt>
) : TooMuchRetryingException(
    uri = uri,
    attempts
)

class TriedForTooLongException(
    uri: String,
    attempts: List<HTTPRequestAttempt>
) : TooMuchRetryingException(
    uri = uri,
    attempts
)

sealed class NoConnectionException(
    uri: String,
    message: String,
    cause: Throwable? = null
) :
    HTTPConnectionProblem(uri = uri, "No Connection: $message", cause = cause)

class HTTPExceptionWhileCreatingConnection(
    uri: String,
    cause: Exception
) :
    NoConnectionException(uri = uri, "Exception while creating connection: ${cause}: ${cause.message}", cause = cause)

class HTTPTimeoutException(
    uri: String,
    duration: Duration
) :
    NoConnectionException(uri = uri, "Timeout after $duration")

sealed class HTTPBadConnectionException(
    uri: String,
    status: Short,
    message: String
) :
    HTTPConnectionProblem(uri = uri, "$status: $message")

class WeirdStatusCodeException(
    uri: String,
    status: Short,
    message: String
) : HTTPBadConnectionException(
    uri = uri,
    status, "is a weird status code. text=${message}"
)

class RedirectionException(
    uri: String,
    status: Short,
    message: String
) :
    HTTPBadConnectionException(uri = uri, status, message)

sealed class HTTPErrorException(
    uri: String,
    status: Short,
    message: String
) :
    HTTPBadConnectionException(uri = uri, status, message)

open class ClientErrorException(
    uri: String,
    status: Short,
    message: String
) :
    HTTPErrorException(uri = uri, status = status, message = message)

class NotFoundException(url: String) : ClientErrorException(uri = url, 404, "Not Found: $url")
class UnsupportedMediaType(
    url: String,
    message: String
) : ClientErrorException(uri = url, 415, "Unsupported Media Type (415): $url: $message")


class UnauthorizedException(
    url: String,
    message: String
) : ClientErrorException(uri = url, 401, message)

open class ServerErrorException(
    uri: String,
    status: Short,
    message: String
) :
    HTTPErrorException(uri = uri, status, message)

class ServiceUnavailableException(
    uri: String,
    message: String
) : ServerErrorException(uri = uri, 401, message)