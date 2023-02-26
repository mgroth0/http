package matt.http.req.write

import java.io.InputStream

interface JDuringConnectionWriter: DuringConnectionWriter {
  val stream: InputStream
}
class StreamBodyWriter(override val stream: InputStream): JDuringConnectionWriter


