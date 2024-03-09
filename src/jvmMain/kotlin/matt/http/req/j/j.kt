
package matt.http.req.j

import matt.http.lib.j.FileBodyWriter
import matt.http.req.MutableHTTPRequest
import matt.lang.model.file.AnyFsFile


fun MutableHTTPRequest.configureForWritingFile(file: AnyFsFile) {
    bodyWriter = FileBodyWriter(file)
}
