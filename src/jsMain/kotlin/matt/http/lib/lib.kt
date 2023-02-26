package matt.http.lib

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js
import io.ktor.http.content.OutgoingContent
import matt.http.req.write.DuringConnectionWriter


actual val httpClientEngine: HttpClientEngineFactory<*> by lazy {
  Js
}


actual fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent {
  TODO("Not yet implemented")
}

