@file:JvmName("LibJvmKt")

package matt.http.lib

import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import matt.http.req.write.DuringConnectionWriter
import matt.lang.anno.SeeURL
import java.io.InputStream


actual fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent {
  return StreamContent((bodyWriter as JDuringConnectionWriter).stream)
}


@SeeURL("https://stackoverflow.com/questions/56706485/in-ktor-how-can-i-stream-an-inputstream-into-a-httpclient-requests-body")
class StreamContent(private val stream: InputStream): OutgoingContent.WriteChannelContent() {
  override suspend fun writeTo(channel: ByteWriteChannel) {
	stream.copyTo(channel, 1024)
  }
}



interface JDuringConnectionWriter: DuringConnectionWriter {
  val stream: InputStream
}
class StreamBodyWriter(override val stream: InputStream): JDuringConnectionWriter


