package matt.http.lib

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import io.ktor.http.content.OutgoingContent
import matt.http.req.write.DuringConnectionWriter


actual val httpClientEngine: HttpClientEngine by lazy {
    Js.create {

    }
}


actual fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent {
    TODO()
}

