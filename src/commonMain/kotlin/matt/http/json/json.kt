package matt.http.json

import io.ktor.http.ContentType.Application
import io.ktor.http.ContentType.Text
import io.ktor.http.withCharset
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.charsets.name
import matt.http.connection.HTTPConnectResult
import matt.http.connection.requireSuccessful
import matt.json.parse
import matt.json.prim.IgnoreUnknownKeysJson
import matt.lang.charset.DEFAULT_CHARSET_NAME_CAP
import matt.prim.str.elementsToString

val KTOR_COMMON_DEFAULT_CHARSET_THING by lazy {
    Charsets.UTF_8.also {
        check(it.name == DEFAULT_CHARSET_NAME_CAP)
    }
}

@Suppress("INLINE_FROM_HIGHER_PLATFORM")
/*this shouldn't be here. Bad module placement, but I'm in a rush for good reasons*/
suspend inline fun <reified T : Any> HTTPConnectResult.requireIs(): T {
    val successfulConnection = requireSuccessful()
    val headers = successfulConnection.headers()

    try {
        val contentType =
            successfulConnection.contentType() ?: return successfulConnection.text().parse<T>()
        return when {
            T::class == String::class && Text.Plain.withCharset(KTOR_COMMON_DEFAULT_CHARSET_THING)
                .match(contentType) -> successfulConnection.text() as T

            T::class == ByteArray::class && Application.OctetStream.match(contentType) -> successfulConnection.bytes() as T

            else -> successfulConnection.text().parse<T>()
        }
    } catch (e: Exception) {
        println(
            "got error parsing HTTP response\n\nHEADERS\n${
                headers.entries().joinToString(separator = "\n") { "\t${it.key}: ${it.value.elementsToString()}" }
            }\n\nBODY\n\n${successfulConnection.text()}\n\n"
        )
        throw e
    }

}


@Suppress("INLINE_FROM_HIGHER_PLATFORM")
suspend inline fun <reified T : Any> HTTPConnectResult.requireIsPartially() =
    requireSuccessful().text().parse<T>(IgnoreUnknownKeysJson)
