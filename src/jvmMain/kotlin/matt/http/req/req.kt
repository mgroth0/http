@file:JvmName("ReqJvmKt")

package matt.http.req

import matt.file.MFile
import matt.http.lib.FileBodyWriter


fun MutableHTTPRequest.configureForWritingFile(file: MFile) {
  bodyWriter = FileBodyWriter(file)
}