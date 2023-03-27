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



actual val httpClientEngine: HttpClientEngine by lazy {
  Java.create {

  }
}