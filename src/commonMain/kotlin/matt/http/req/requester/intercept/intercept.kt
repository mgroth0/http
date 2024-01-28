package matt.http.req.requester.intercept

import matt.http.connection.HTTPConnection
import matt.http.connection.HTTPConnectionProblem
import matt.http.connection.HTTPConnectionProblemNoResponse
import matt.http.connection.HTTPConnectionProblemWithResponse
import matt.http.connection.MultipleHTTPConnectResult
import matt.http.connection.SingleHTTPConnectResult
import matt.http.report.EndSide.Client
import matt.http.report.HTTPRequestReport
import matt.http.req.ImmutableHTTPRequest
import matt.http.req.requester.problems.ClientErrorException
import matt.http.req.requester.problems.HttpExceptionDataNoMessage
import matt.http.req.requester.problems.NotFoundException
import matt.http.req.requester.problems.RedirectionException
import matt.http.req.requester.problems.ServerErrorException
import matt.http.req.requester.problems.ServiceUnavailableException
import matt.http.req.requester.problems.UnauthorizedException
import matt.http.req.requester.problems.UnsupportedMediaType
import matt.http.req.requester.problems.WeirdStatusCodeException
import matt.lang.anno.SeeURL
import matt.lang.inList
import matt.model.code.errreport.throwReport
import matt.time.nowKotlinDateTime


interface HttpConnectionInterceptor {
    suspend fun intercept(
        request: ImmutableHTTPRequest,
        connection: SingleHTTPConnectResult
    ): SingleHTTPConnectResult
}

abstract class ErrorCheckingInterceptor(
    private val suppressAnyErrorReport: Boolean
) : HttpConnectionInterceptor {


    final override suspend fun intercept(
        request: ImmutableHTTPRequest,
        connection: SingleHTTPConnectResult
    ): SingleHTTPConnectResult {
        return when (connection) {
            is HTTPConnection                    -> {
                interceptConnection(request, connection)
            }

            is HTTPConnectionProblem             -> reportAndThrowConnectionProblem(connection, request)
            is HTTPConnectionProblemWithResponse -> reportAndThrowConnectionProblem(connection, request)
            is HTTPConnectionProblemNoResponse   -> reportAndThrowConnectionProblem(connection, request)
        }
    }

    private fun reportAndThrowConnectionProblem(
        result: HTTPConnectionProblem,
        request: ImmutableHTTPRequest
    ): Nothing {
        reportConnectionProblem(result, request)
        throw result
    }

    private fun reportConnectionProblem(
        result: HTTPConnectionProblem,
        request: ImmutableHTTPRequest
    ) {
        val date = nowKotlinDateTime()
        if (!suppressAnyErrorReport) {
            val report = HTTPRequestReport(side = Client,
                date = date,
                uri = request.url,
                method = request.method.name,
                headers = request.headers.groupBy { it.first }.mapValues { it.value.map { it.second } },
                attributes = when (result) {
                    is SingleHTTPConnectResult   -> result.requestAttributes.inList()
                    is MultipleHTTPConnectResult -> result.requestAttributes
                },
                parameters = mapOf() /*not sure if parameters are available from the client side*/,
                throwReport = throwReport(result)
            )
            report.print()
        }
    }

    abstract suspend fun interceptConnection(
        request: ImmutableHTTPRequest,
        connection: HTTPConnection
    ): SingleHTTPConnectResult
}

@SeeURL("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status")
class StatusCodeChecker(
    suppressAnyErrorReport: Boolean
) : ErrorCheckingInterceptor(suppressAnyErrorReport) {

    override suspend fun interceptConnection(
        request: ImmutableHTTPRequest,
        connection: HTTPConnection
    ): SingleHTTPConnectResult {
        suspend fun exceptionData() = HttpExceptionDataNoMessage(
            uri = request.url,
            status = connection.statusCode(),
            headers = connection.headers(),
            responseBody = connection.text(),
            requestAttributes = connection.requestAttributes
        )

        val statusCodeObj = connection.statusCode()
        val newResult = when (val statusCode = statusCodeObj.value.toShort()) {
            in 300..399  -> {
                RedirectionException(
                    exceptionData().withBodyAsMessage()
                )
            }

            in 400..499  -> when (statusCode.toInt()) {
                401  -> UnauthorizedException(
                    exceptionData().withBodyAsMessage()
                )

                404  -> NotFoundException(
                    exceptionData()
                )

                415  -> UnsupportedMediaType(
                    exceptionData().withBodyAsMessage()
                )

                else -> ClientErrorException(
                    exceptionData().withBodyAsMessage()
                )
            }

            in 500..599  -> when (statusCode.toInt()) {
                503  -> ServiceUnavailableException(
                    exceptionData().withBodyAsMessage()
                )

                else -> ServerErrorException(
                    exceptionData().withBodyAsMessage()
                )
            }

            !in 100..300 -> WeirdStatusCodeException(
                exceptionData().withBodyAsMessage()
            )

            else         -> connection
        }
        return newResult
    }


}