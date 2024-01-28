@file:JvmName("ReqJvmKt")

package matt.http.req

import matt.http.lib.FileBodyWriter
import matt.lang.model.file.AnyFsFile


fun MutableHTTPRequest.configureForWritingFile(file: AnyFsFile) {
    bodyWriter = FileBodyWriter(file)
}