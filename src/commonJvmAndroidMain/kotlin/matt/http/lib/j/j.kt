package matt.http.lib.j

import io.ktor.http.content.OutgoingContent.WriteChannelContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import matt.http.req.write.DuringConnectionWriter
import matt.lang.anno.SeeURL
import matt.lang.model.file.AnyFsFile
import java.io.InputStream

@SeeURL("https://stackoverflow.com/questions/56706485/in-ktor-how-can-i-stream-an-inputstream-into-a-httpclient-requests-body")
class StreamContent(private val stream: InputStream) : WriteChannelContent() {
    override suspend fun writeTo(channel: ByteWriteChannel) {
        stream.copyTo(channel, 1024)
    }
}

sealed interface JDuringConnectionWriter : DuringConnectionWriter
class StreamBodyWriter(val stream: InputStream) : JDuringConnectionWriter
class FileBodyWriter(val file: AnyFsFile) : JDuringConnectionWriter
