package matt.http.req.requester.problems

import io.ktor.http.*
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPConnectionProblem
import matt.http.connection.HTTPConnectionProblemWithResponse
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
    status: HttpStatusCode,
    message: String,
    headers: Headers,
    responseBody: String
) : HTTPConnectionProblemWithResponse(
    uri = uri,
    "$status: $message",
    status = status,
    headers = headers,
    responseBody = responseBody
)

class WeirdStatusCodeException(
    uri: String,
    status: HttpStatusCode,
    message: String,
    headers: Headers,
    responseBody: String
) : HTTPBadConnectionException(
    uri = uri,
    status = status,
    message = "is a weird status code. text=${message}", headers = headers, responseBody = responseBody
)

class RedirectionException(
    uri: String,
    status: HttpStatusCode,
    message: String,
    headers: Headers,
    responseBody: String
) :
    HTTPBadConnectionException(uri = uri, status, message, headers = headers, responseBody = responseBody)

sealed class HTTPErrorException(
    uri: String,
    status: HttpStatusCode,
    message: String,
    headers: Headers,
    responseBody: String
) :
    HTTPBadConnectionException(uri = uri, status, message, headers = headers, responseBody = responseBody)

open class ClientErrorException(
    uri: String,
    status: HttpStatusCode,
    message: String,
    headers: Headers,
    responseBody: String
) :
    HTTPErrorException(uri = uri, status = status, message = message, headers = headers, responseBody = responseBody)

class NotFoundException(
    url: String,
    headers: Headers,
    responseBody: String
) : ClientErrorException(
    uri = url,
    HttpStatusCode.NotFound,
    "Not Found: $url",
    headers = headers,
    responseBody = responseBody
)

class UnsupportedMediaType(
    url: String,
    message: String,
    headers: Headers,
    responseBody: String
) : ClientErrorException(
    uri = url,
    HttpStatusCode.UnsupportedMediaType,
    "Unsupported Media Type (415): $url: $message",
    headers = headers,
    responseBody = responseBody
)


class UnauthorizedException(
    url: String,
    message: String,
    headers: Headers,
    responseBody: String
) : ClientErrorException(
    uri = url,
    HttpStatusCode.Unauthorized,
    message,
    headers = headers,
    responseBody = responseBody
)

open class ServerErrorException(
    uri: String,
    status: HttpStatusCode,
    message: String,
    headers: Headers,
    responseBody: String
) :
    HTTPErrorException(uri = uri, status, message, headers = headers, responseBody = responseBody)

class ServiceUnavailableException(
    uri: String,
    message: String,
    headers: Headers,
    responseBody: String
) : ServerErrorException(
    uri = uri,
    HttpStatusCode.ServiceUnavailable,
    message,
    headers = headers,
    responseBody = responseBody
)