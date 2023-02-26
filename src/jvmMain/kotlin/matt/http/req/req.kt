package matt.http.req

import matt.async.future.future
import matt.file.FileOrURL
import matt.file.MFile
import matt.http.connection.ConnectionRefused
import matt.http.connection.HTTPAsyncConnection
import matt.http.connection.HTTPConnectResult
import matt.http.connection.JHTTPConnection
import matt.http.connection.Timeout
import matt.http.connection.async.JHTTPAsyncConnection
import matt.http.live.JLiveHTTPConnection
import matt.http.req.write.AsyncWriter
import matt.http.req.write.BasicHTTPWriter
import matt.lang.sync
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpRequest.Builder
import java.net.http.HttpResponse.BodyHandlers


actual class HTTPTemplateImpl internal actual constructor(override val url: FileOrURL) {

  private var didConnect = false

  @Synchronized actual override fun openConnection(): HTTPConnectResult {
	require(!didConnect)
	didConnect = true
	return try {

	  val client = HttpClient.newBuilder().build()
	  val response = client.sendAsync(jCon, BodyHandlers.ofInputStream())

	  if (liveConnectionOps.isNotEmpty()) {
		sync {
		  val liveConnection = JLiveHTTPConnection(outputStream)
		  liveConnectionOps.forEach {
			liveConnection.it()
		  }
		  liveConnection.outputStream.close()
		  didLiveConnectionOps = true
		}
	  }


	  JHTTPConnection(response.get())


	} catch (e: SocketTimeoutException) {
	  return Timeout
	} catch (e: ConnectException) {
	  if (e.message?.trim() == "Connection refused") return ConnectionRefused
	  else throw e
	}
  }


}


