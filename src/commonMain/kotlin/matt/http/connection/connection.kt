package matt.http.connection

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import matt.log.todo.todo


sealed interface HTTPConnectResult

abstract class HTTPConnectionProblem(message: String): Exception(message), HTTPConnectResult

class HTTPConnection(private val response: HttpResponse): HTTPConnectResult {

  init {
	todo("file.readChannel().efficientlyTransferTo(liveHTTPConnection.outputStream)")
  }

  private var alreadyReadBytes: ByteArray? = null
  private val mutex = Mutex()

  suspend fun bytes(): ByteArray {
	mutex.withLock {
	  if (alreadyReadBytes == null) {
		alreadyReadBytes = response.readBytes()
	  }
	  return alreadyReadBytes!!
	}
  }

  suspend fun text(): String {
	return bytes().decodeToString()
  }

  private var alreadyGotHTTPStatusCode: HttpStatusCode? = null
  private val statusMutex = Mutex()

  suspend fun statusCode(): HttpStatusCode {
	statusMutex.withLock {
	  if (alreadyGotHTTPStatusCode == null) {
		alreadyGotHTTPStatusCode = response.status
	  }
	  return alreadyGotHTTPStatusCode!!
	}
  }

  suspend fun requireSuccessful(): HTTPConnection {
	bytes()
	return this
  }

}


