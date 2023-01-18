package matt.http.req.write

import matt.file.MFile
import matt.http.req.HTTPRequestImpl
import matt.prim.byte.efficientlyTransferTo
import kotlin.concurrent.thread


class BasicHTTPWriter(
  private val req: HTTPRequestImpl,
  private val bytes: ByteArray
): HTTPWriter() {
  override fun write() {
	req.writeBytesNow(bytes)
  }
}

class AsyncWriter(
  private val req: HTTPRequestImpl,
  private val file: MFile
): HTTPWriter() {
  override fun write() {
	thread {
	  println("running async data transfer")
	  file.readChannel().efficientlyTransferTo(req.outputStream)
	  println("finished running async data transfer")
	}
  }
}