package matt.http.json

import io.ktor.http.*
import io.ktor.http.ContentType.*
import io.ktor.utils.io.charsets.*
import matt.http.connection.HTTPConnectResult
import matt.http.connection.requireSuccessful
import matt.json.parse
import matt.json.prim.IgnoreUnknownKeysJson
import matt.prim.str.elementsToString

/*this shouldn't be here. Bad module placement, but I'm in a rush for good reasons*/
suspend inline fun <reified T : Any> HTTPConnectResult.requireIs(): T {
    val successfulConnection = requireSuccessful()
    val headers = successfulConnection.headers()

    try {
        val contentType = successfulConnection.contentType() ?: return successfulConnection.text().parse<T>()
        return when {
            T::class == String::class && Text.Plain.withCharset(Charsets.UTF_8)
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


suspend inline fun <reified T : Any> HTTPConnectResult.requireIsPartially() =
    requireSuccessful().text().parse<T>(IgnoreUnknownKeysJson)