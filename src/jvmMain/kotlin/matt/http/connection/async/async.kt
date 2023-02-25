package matt.http.connection.async

import matt.http.connection.HTTPAsyncConnection
import matt.http.connection.HTTPConnectResult
import matt.lang.function.Consume
import kotlin.concurrent.thread

class JHTTPAsyncConnection(
  private val resultGetter: ()->HTTPConnectResult
): HTTPAsyncConnection {

  override fun whenDone(op: Consume<HTTPConnectResult>) {
	/*obviously this can be done way more efficiently*/
	thread {
	  op(resultGetter())
	}
  }

}