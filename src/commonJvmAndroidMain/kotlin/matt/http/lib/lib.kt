@file:JvmName("LibJvmAndroidKt")

package matt.http.lib

//import io.ktor.client.engine.java.*
import io.ktor.client.content.LocalFileContent
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import matt.http.req.write.DuringConnectionWriter
import matt.lang.anno.SeeURL
import matt.lang.file.toJFile
import matt.lang.model.file.AnyFsFile
import java.io.InputStream

//
//actual val httpClientEngine: HttpClientEngine by lazy {
//    Java.create {
//        /*the default was http 1.1 and I think this was making heroku angry?*/
//        this.protocolVersion = HTTP_2
//    }
//}


actual fun figureOutLiveContentWriter(bodyWriter: DuringConnectionWriter): OutgoingContent {
    return when (val jWriter = (bodyWriter as JDuringConnectionWriter)) {
        is StreamBodyWriter -> StreamContent(jWriter.stream)
        is FileBodyWriter   -> LocalFileContent(jWriter.file.toJFile())
    }
}


@SeeURL("https://stackoverflow.com/questions/56706485/in-ktor-how-can-i-stream-an-inputstream-into-a-httpclient-requests-body")
class StreamContent(private val stream: InputStream) : OutgoingContent.WriteChannelContent() {
    override suspend fun writeTo(channel: ByteWriteChannel) {
        stream.copyTo(channel, 1024)
    }
}


sealed interface JDuringConnectionWriter : DuringConnectionWriter
class StreamBodyWriter(val stream: InputStream) : JDuringConnectionWriter
class FileBodyWriter(val file: AnyFsFile) : JDuringConnectionWriter