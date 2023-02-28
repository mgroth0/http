@file:JvmName("LibJvmKt")

package matt.http.lib

import io.ktor.client.content.LocalFileContent
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import matt.file.MFile
import matt.http.req.write.DuringConnectionWriter
import matt.lang.anno.SeeURL
import java.io.InputStream


actual fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent {
  return when (val jWriter = (bodyWriter as JDuringConnectionWriter)) {
	is StreamBodyWriter -> StreamContent(jWriter.stream)
	is FileBodyWriter   -> LocalFileContent(jWriter.file)
  }
}


@SeeURL("https://stackoverflow.com/questions/56706485/in-ktor-how-can-i-stream-an-inputstream-into-a-httpclient-requests-body")
class StreamContent(private val stream: InputStream): OutgoingContent.WriteChannelContent() {
  override suspend fun writeTo(channel: ByteWriteChannel) {
	stream.copyTo(channel, 1024)
  }
}


sealed interface JDuringConnectionWriter: DuringConnectionWriter
class StreamBodyWriter(val stream: InputStream): JDuringConnectionWriter
class FileBodyWriter(val file: MFile): JDuringConnectionWriter

actual val httpClientEngine: HttpClientEngine by lazy {
  Java.create {

  }
}