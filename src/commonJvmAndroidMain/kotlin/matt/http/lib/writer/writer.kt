package matt.http.lib.writer

import io.ktor.client.content.LocalFileContent
import io.ktor.http.content.OutgoingContent
import matt.http.lib.j.FileBodyWriter
import matt.http.lib.j.JDuringConnectionWriter
import matt.http.lib.j.StreamBodyWriter
import matt.http.lib.j.StreamContent
import matt.http.req.write.DuringConnectionWriter
import matt.lang.file.toJFile


actual fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent =
    when (val jWriter = (bodyWriter as JDuringConnectionWriter)) {
        is StreamBodyWriter -> StreamContent(jWriter.stream)
        is FileBodyWriter   -> LocalFileContent(jWriter.file.toJFile())
    }


