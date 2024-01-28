package matt.http.req

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.http.HTTPDslMarker
import matt.http.headers.content.HTTPMediaType.applicationJsonCharsetUTF8
import matt.http.headers.content.HTTPMediaType.textPlain
import matt.http.headers.headers
import matt.http.method.HTTPMethod
import matt.http.method.HTTPMethod.GET
import matt.http.req.write.BodyWriter
import matt.http.req.write.BytesBodyWriter
import matt.http.req.write.NoBody
import matt.http.url.MURL
import matt.http.url.query.withPort
import matt.http.url.query.withQueryParams
import matt.lang.ILLEGAL
import matt.lang.anno.SeeURL

interface HasHeaders {
    fun headersSnapshot(): List<Pair<String, String>>
}

@HTTPDslMarker
interface HTTPRequest : HasHeaders {
    companion object {
        val EXAMPLE by lazy {
            ImmutableHTTPRequest(
                url = "https://example.com/",
                method = GET,
                headers = listOf(),
                bodyWriter = NoBody
            )
        }
    }

    val url: String
    val method: HTTPMethod
    override fun headersSnapshot(): List<Pair<String, String>>
    val bodyWriter: BodyWriter
    fun snapshot(): ImmutableHTTPRequest
}

open class MutableHeaders : HasHeaders {
    private val headerList = mutableListOf<Pair<String, String>>()
    fun addHeader(
        key: String,
        value: String
    ) {
        headerList += key to value
    }

    final override fun headersSnapshot() = headerList.toList()
}


fun HasHeaders.valueForHeader(key: String): String? {
    val matchingPairs = headersSnapshot().filter { it.first == key }
    return when (matchingPairs.size) {
        0    -> null
        1    -> matchingPairs.single().second
        else -> error("do not know how to handle this")
    }
}


@SeeURL("https://youtrack.jetbrains.com/issue/KT-20427")
class MutableHTTPRequest : MutableHeaders(), HTTPRequest {
    override var url = ""
    override var method = GET
    override var bodyWriter: BodyWriter = NoBody

    fun configureForWritingBytes(bytes: ByteArray) {
        bodyWriter = BytesBodyWriter(bytes)
    }

    var data: ByteArray
        get() = ILLEGAL
        set(value) {
            configureForWritingBytes(value)
        }


    fun configureForWritingString(string: String) {
        configureForWritingBytes(string.encodeToByteArray())
        headers {
            contentType = textPlain
        }
    }

    inline fun <reified T> configureForWritingJson(someData: T) {
        configureForWritingBytes(Json.encodeToString(someData).encodeToByteArray())
        headers {
            contentType = applicationJsonCharsetUTF8
        }
    }


    override fun snapshot() = ImmutableHTTPRequest(
        url = url,
        method = method,
        headers = headersSnapshot(),
        bodyWriter = bodyWriter
    )

    fun queryParam(
        key: String,
        value: String
    ) {
        val murl = MURL(url)
        url = murl.withQueryParams(mapOf(key to value)).path
    }

    fun port(p: Int) {
        url = MURL(url).withPort(p).path
    }

}

data class ImmutableHTTPRequest(
    override val url: String,
    override val method: HTTPMethod,
    val headers: List<Pair<String, String>>,
    override val bodyWriter: BodyWriter
) : HTTPRequest {
    override fun headersSnapshot(): List<Pair<String, String>> {
        return headers
    }

    override fun snapshot() = this
}


