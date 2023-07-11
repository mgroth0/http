package matt.http.lib

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*
import io.ktor.http.content.*
import matt.http.req.write.DuringConnectionWriter


actual val httpClientEngine: HttpClientEngine by lazy {
  Js.create {

  }
}


actual fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent {
  TODO("Not yet implemented")
}

