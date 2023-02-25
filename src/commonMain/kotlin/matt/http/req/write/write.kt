package matt.http.req.write

import matt.http.req.HTTPRequestImpl
import matt.lang.ILLEGAL

var HTTPRequestImpl.data: ByteArray
  get() = ILLEGAL
  set(value) {
    configureForWritingBytes(value)
  }

abstract class HTTPWriter {
  internal abstract fun write()
}

