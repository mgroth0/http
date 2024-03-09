package matt.http.sse

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.sse.ClientSSESession
import io.ktor.client.plugins.sse.DefaultClientSSESession
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.SSEClientContent
import io.ktor.client.plugins.sse.SSESession
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.ResponseAdapter
import io.ktor.client.request.ResponseAdapterAttributeKey
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Text
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.sse.SSEException
import io.ktor.sse.ServerSentEvent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import matt.lang.common.forever
import matt.model.code.errreport.j.ThrowReport
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration


class ServerSideEventsClient {






    @OptIn(InternalAPI::class)
    private val client by lazy {
        run {
            val plugin =
                createClientPlugin("debug") {
                    client.responsePipeline.intercept(HttpResponsePipeline.Transform) { (info, session) ->
                        val response = context.response
                        val status = response.status
                        val contentType = response.contentType()
                        /*val requestContent = response.request.content

                        if (requestContent !is SSEClientContent) {
                        return@intercept
                    }*/
                        if (status != HttpStatusCode.OK) {
                            throw SSEException("Expected status code ${HttpStatusCode.OK.value} but was: ${status.value}")
                        }
                        if (contentType?.withoutParameters() != Text.EventStream) {
                            throw SSEException("Expected Content-Type ${Text.EventStream} but was: $contentType")
                        }
                        if (session !is SSESession) {
                            throw SSEException("Expected ${SSESession::class.simpleName} content but was: $session")
                        }

                        proceedWith(HttpResponseContainer(info, ClientSSESession(context, session)))
                    }
                }
            HttpClient(Java) {

                /*

            Using my own fork TEMPORARILY because ktor recently made major fixes to the plugin in an unreleased version...

                 */
                install(SSE) {
                    showCommentEvents()
                    showRetryEvents()
                }

                install(plugin)
            }.apply {
                plugin(HttpSend).intercept { request ->
                    /*THIS COMBINATION OF HEADERS MIGHT ONLY WORK FOR HEROKU / MIGHT ONLY BE NEEDED FOR HEROKU*/
                    request.headers.apply {
                        remove("Accept-Charset")
                        append("Cache-Control", "no-cache") /* THIS MIGHT ONLY WORK FOR HEROKU. IT THINK IT IS NOT CORRECT ACCORDING TO THE OFFICIAL SSE SPECIFICATION */
                        remove("Accept")
                        append("Accept", "text/event-stream")
                    }
                    request.attributes.put(
                        ResponseAdapterAttributeKey,
                        ForcedSSEClientResponseAdapter(request.body as SSEClientContent)
                    )
                    request.body = EmptyContent
                    execute(request)
                }
            }
        }
    }

    suspend fun sse(
        url: String,
        headers: HeadersBuilder.() -> Unit,
        allowRetryEvery: Duration?,
        consume: suspend (event: ServerSideEventEvent) -> Unit
    ) {
        coroutineScope {
            val retryTokens = AtomicInteger(0)
            if (allowRetryEvery != null) {
                launch {
                    forever {
                        delay(allowRetryEvery)
                        retryTokens.incrementAndGet()
                    }
                }
            }
            do {
                var allowRetry = false
                try {
                    sse(url, headers, consume)
                } catch (e: IOException) {

                    val tokensLeft = retryTokens.decrementAndGet()
                    val gotToken = tokensLeft >= 0

                    println("Got IOException in SSE Session. tokensLeft=$tokensLeft, gotToken=$gotToken")



                    if (gotToken) {
                        println("Printing exception below and then retrying connection.")
                        ThrowReport(e).print()
                        allowRetry = true
                        consume(ClientReconnecting)
                    } else {
                        println("No more retry tokens left. Throwing.")
                        throw e
                    }

                    /*

                    Heroku Log Session is throwing IOException and EOFException after viewing log for a long time (an hour or two). This behavior seems undocumented, but it matches what I have observed on the command line where I have to periodically restart the log tail. They probably do this to preserve resources.

                     */
                }
            } while (allowRetry)
        }
    }
    private suspend fun sse(
        url: String,
        headers: HeadersBuilder.() -> Unit,
        consume: suspend (event: ServerSideEvent) -> Unit
    ) {
        client.sse(
            request = {
                headers(headers)
            },
            urlString = url
        ) {



            /* official example seems to use a while(true) loop. Not sure why.*/
            while (true) {
                incoming.collect { rawEvent: ServerSentEvent ->

                    /*
                     * retry has so far always been null for me, so I ignore it
                     */
                    check(rawEvent.retry == null)
                    /*
                     * id has the following possible uses
                     * duplicate detection
                     * I do this
                     * ensure proper ordering
                     * I will not do this for now, because I am unsure it will work or if it is needed
                     * reconnection (tell server 'Last-Event-ID')
                     * I will not do this for now because I am unsure if ktor does it under the hood
                     * App-specific uses
                     * I don't know of any yet

                     * * ...
                     * * JUST DISCOVERED THAT A SERIES OF EVENTS WITH DIFFERENT DATA CAN HAVE THE SAME ID, AT LEAST FROM HEROKU.
                     * * This logically means the ID must not be checked for duplicates.
                     * * Rather, its use must be mainly for last-event-id, if anything. It might just have internal uses.
                     * * Therefore I will just check not null for the sake of being a bit more robust, but otherwise will ignore it.
                     * * Technically I don't need to check if the ID is notNull since I do not use it. But if I come back here in the future
                     * * and see that it is always non-null, that might be helpful.
                     */
                    val id = rawEvent.id
                    checkNotNull(id)
                    /*if (!idsGot.add(id)) {
                        println("WARNING: DUPLICATE ID ($id): $rawEvent")
                    }*/

                    val event = rawEvent.event
                    val data = rawEvent.data
                    val comments = rawEvent.comments

                    val processedEvent =
                        when {
                            /*I am getting weird blank comments sometimes*/
                            comments?.isNotBlank() == true -> {
                                check(data == null) {
                                    "got data with a comment? $rawEvent"
                                }
                                check(event == null)
                                Comment(comments)
                            }
                            else ->
                                when (event) {
                                    null -> JustData(data!!)
                                    else -> error("Not prepared for event with type $event: $rawEvent")
                                }
                        }
                    consume(processedEvent)
                }
            }
        }
    }
}

sealed interface ServerSideEventEvent
data object ClientReconnecting: ServerSideEventEvent

sealed interface ServerSideEvent: ServerSideEventEvent
class Comment(val comment: String): ServerSideEvent
class JustData(val data: String): ServerSideEvent




@OptIn(InternalAPI::class)
private class ForcedSSEClientResponseAdapter(
    private val clientContent: SSEClientContent
) : ResponseAdapter {
    override fun adapt(
        data: HttpRequestData,
        status: HttpStatusCode,
        headers: Headers,
        responseBody: ByteReadChannel,
        outgoingContent: OutgoingContent,
        callContext: CoroutineContext
    ): Any? {
        val contentType = headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
        return if (/*data.isSseRequest() &&*/
            status == HttpStatusCode.OK &&
            contentType?.withoutParameters() == Text.EventStream
        ) {
            DefaultClientSSESession(
                clientContent,
                responseBody,
                callContext
            )
        } else {
            null
        }
    }
}



