package matt.http.req

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.http.HTTPDslMarker
import matt.http.headers.HTTPMediaType.applicationJsonCharsetUTF8
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

@HTTPDslMarker
interface HTTPRequest {
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
    fun headersSnapshot(): List<Pair<String, String>>
    val bodyWriter: BodyWriter
    fun snapshot(): ImmutableHTTPRequest
}

fun HTTPRequest.valueForHeader(key: String): String? {
    val matchingPairs = headersSnapshot().filter { it.first == key }
    return when (matchingPairs.size) {
        0    -> null
        1    -> matchingPairs.single().second
        else -> error("do not know how to handle this")
    }
}


@SeeURL("https://youtrack.jetbrains.com/issue/KT-20427")
class MutableHTTPRequest : HTTPRequest {
    override var url = ""
    override var method = GET
    override var bodyWriter: BodyWriter = NoBody
    private val headerList = mutableListOf<Pair<String, String>>()
    fun addHeader(key: String, value: String) {
        headerList += key to value
    }

    override fun headersSnapshot() = headerList.toList()


    fun configureForWritingBytes(bytes: ByteArray) {
        bodyWriter = BytesBodyWriter(bytes)
    }

    var data: ByteArray
        get() = ILLEGAL
        set(value) {
            configureForWritingBytes(value)
        }


    fun configureForWritingString(string: String) = configureForWritingBytes(string.encodeToByteArray())

    inline fun <reified T> configureForWritingJson(someData: T) {
        headers {
            contentType = applicationJsonCharsetUTF8
        }
        configureForWritingString(Json.encodeToString(someData))
    }


    override fun snapshot() = ImmutableHTTPRequest(
        url = url,
        method = method,
        headers = headersSnapshot(),
        bodyWriter = bodyWriter
    )

    fun queryParam(key: String, value: String) {
        val murl = MURL(url)
        url = murl.withQueryParams(mapOf(key to value)).cpath
    }

    fun port(p: Int) {
        url = MURL(url).withPort(p).cpath
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


