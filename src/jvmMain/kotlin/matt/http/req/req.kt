@file:JvmName("ReqJvmKt")

package matt.http.req

import matt.http.lib.FileBodyWriter
import matt.lang.model.file.FsFile


fun MutableHTTPRequest.configureForWritingFile(file: FsFile) {
    bodyWriter = FileBodyWriter(file)
}