@file:JvmName("ReqJvmKt")

package matt.http.req

import matt.file.JioFile
import matt.http.lib.FileBodyWriter


fun MutableHTTPRequest.configureForWritingFile(file: JioFile) {
  bodyWriter = FileBodyWriter(file)
}