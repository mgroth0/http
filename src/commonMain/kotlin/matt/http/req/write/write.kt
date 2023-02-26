package matt.http.req.write

sealed interface BodyWriter
object NoBody: BodyWriter
class BytesBodyWriter(val bytes: ByteArray): BodyWriter
interface DuringConnectionWriter: BodyWriter


