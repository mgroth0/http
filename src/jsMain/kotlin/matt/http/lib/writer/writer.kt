package matt.http.lib.writer

import io.ktor.http.content.OutgoingContent
import matt.http.req.write.DuringConnectionWriter


actual fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent {
    TODO()
}
