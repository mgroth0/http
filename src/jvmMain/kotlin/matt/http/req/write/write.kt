package matt.http.req.write

import matt.file.MFile
import matt.http.live.JLiveHTTPConnection
import matt.prim.byte.efficientlyTransferTo
import kotlin.concurrent.thread


class BasicHTTPWriter(
  private val liveHTTPConnection: JLiveHTTPConnection,
  private val bytes: ByteArray
): HTTPWriter() {
  override fun write() {
	liveHTTPConnection.writeBytesNow(bytes)
  }
}

class AsyncWriter(
  private val liveHTTPConnection: JLiveHTTPConnection,
  private val file: MFile
): HTTPWriter() {
  override fun write() {
	thread {
	  println("running async data transfer")
	  file.readChannel().efficientlyTransferTo(liveHTTPConnection.outputStream)
	  println("finished running async data transfer")
	}
  }
}