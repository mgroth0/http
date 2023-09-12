package matt.http.req.requester.problems

import io.ktor.http.*
import io.ktor.util.*
import matt.http.connection.HTTPConnectionProblemNoResponse
import matt.http.connection.HTTPConnectionProblemWithMultipleRequests
import matt.http.connection.HTTPConnectionProblemWithResponse
import matt.http.connection.SingleHTTPConnectResult
import matt.model.code.errreport.infoString
import matt.prim.str.joinWithNewLines
import matt.prim.str.maybePlural
import kotlin.time.Duration


class HTTPRequestAttempt(
    val tSent: Duration,
    val tGotResult: Duration,
    val result: SingleHTTPConnectResult
)

sealed class TooMuchRetryingException(
    uri: String,
    attempts: List<HTTPRequestAttempt>,
    problems: List<Exception>
) : HTTPConnectionProblemWithMultipleRequests(uri = uri,
    "No successful connection after ${attempts.size} ${
        maybePlural(
            attempts.size, "attempt"
        )
    }\n${attempts.joinWithNewLines { "\t${it.tSent}-${it.tGotResult}\t${it.result}" }}\n\n\n" + problems.joinWithNewLines { it.infoString() },
    requestAttributes = attempts.map { it.result.requestAttributes })

class TooManyRetrysException(
    uri: String,
    attempts: List<HTTPRequestAttempt>,
    problems: List<Exception>
) : TooMuchRetryingException(
    uri = uri, attempts, problems
)

class TriedForTooLongException(
    uri: String,
    attempts: List<HTTPRequestAttempt>,
    problems: List<Exception>
) : TooMuchRetryingException(
    uri = uri, attempts, problems
)

sealed class NoConnectionException(
    uri: String,
    message: String,
    requestAttributes: Attributes,
    cause: Throwable? = null
) : HTTPConnectionProblemNoResponse(
    uri = uri, "No Connection: $message", cause = cause, requestAttributes = requestAttributes
)

class HTTPExceptionWhileCreatingConnection(
    uri: String,
    cause: Exception,
    requestAttributes: Attributes,
) : NoConnectionException(
    uri = uri,
    "Exception while creating connection: ${cause}: ${cause.message}",
    cause = cause,
    requestAttributes = requestAttributes
)

class HTTPTimeoutException(
    uri: String,
    duration: Duration,
    requestAttributes: Attributes,
) : NoConnectionException(uri = uri, "Timeout after $duration", requestAttributes = requestAttributes)

sealed class HTTPBadConnectionException(
    uri: String,
    status: HttpStatusCode,
    message: String,
    headers: Headers,
    responseBody: String,
    requestAttributes: Attributes
) : HTTPConnectionProblemWithResponse(
    uri = uri,
    message = "$status: $message",
    status = status,
    headers = headers,
    responseBody = responseBody,
    requestAttributes = requestAttributes
) {
    constructor(exceptionData: HttpExceptionData) : this(
        uri = exceptionData.uri,
        status = exceptionData.status,
        message = exceptionData.message,
        headers = exceptionData.headers,
        responseBody = exceptionData.responseBody,
        requestAttributes = exceptionData.requestAttributes
    )
}

data class HttpExceptionDataNoMessage(
    val uri: String,
    val status: HttpStatusCode,
    val headers: Headers,
    val responseBody: String,
    val requestAttributes: Attributes
) {
    fun withMessage(m: String) = HttpExceptionData(
        uri = uri,
        status = status,
        message = m,
        headers = headers,
        responseBody = responseBody,
        requestAttributes = requestAttributes
    )
    fun withBodyAsMessage() = withMessage(responseBody)
}

data class HttpExceptionData(
    val uri: String,
    val status: HttpStatusCode,
    val message: String,
    val headers: Headers,
    val responseBody: String,
    val requestAttributes: Attributes
)


class WeirdStatusCodeException(
    exceptionData: HttpExceptionData
) : HTTPBadConnectionException(exceptionData.copy(message = "is a weird status code. text=${exceptionData.message}"))

class RedirectionException(
    exceptionData: HttpExceptionData
) : HTTPBadConnectionException(exceptionData)

sealed class HTTPErrorException(val exceptionData: HttpExceptionData) : HTTPBadConnectionException(exceptionData)

open class ClientErrorException(
    exceptionData: HttpExceptionData
) : HTTPErrorException(exceptionData)

class NotFoundException(
    exceptionData: HttpExceptionDataNoMessage
) : ClientErrorException(exceptionData.withMessage("Not Found: ${exceptionData.uri}"))

class UnsupportedMediaType(
    exceptionData: HttpExceptionData
) : ClientErrorException(
    exceptionData.copy(message = "Unsupported Media Type (415): ${exceptionData.uri}: ${exceptionData.message}")
)


class UnauthorizedException(
    exceptionData: HttpExceptionData
) : ClientErrorException(exceptionData.also { require(it.status == HttpStatusCode.Unauthorized) })

open class ServerErrorException(
    exceptionData: HttpExceptionData
) : HTTPErrorException(exceptionData)

class ServiceUnavailableException(
    exceptionData: HttpExceptionData
) : ServerErrorException(
    exceptionData.also { require(it.status == HttpStatusCode.ServiceUnavailable) }
)