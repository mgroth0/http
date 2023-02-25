package matt.http.req

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.file.FileOrURL
import matt.http.HTTPDslMarker
import matt.http.connection.HTTPAsyncConnection
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPResponse
import matt.http.headers.HTTPMediaType.applicationJsonCharsetUTF8
import matt.http.headers.headers
import matt.http.method.HTTPMethod
import matt.lang.anno.SeeURL
import matt.log.tab
import kotlin.time.Duration

@HTTPDslMarker
@SeeURL("https://youtrack.jetbrains.com/issue/KT-20427")
abstract class HTTPRequest {
  protected abstract val url: FileOrURL
  var verbose = false
  abstract var method: HTTPMethod
  abstract fun getRequestProperty(name: String): String?
  abstract fun setRequestProperty(name: String, value: String?)
  abstract fun allRequestHeaders(): Map<String, List<String>>
  protected abstract fun openConnection(): HTTPConnectResult
  protected abstract fun openAsyncConnection(): HTTPAsyncConnection
  private fun printInfo() {
	println("sending ${method.name} to $url")
	println("request properties:")
	allRequestHeaders().forEach {
	  tab("${it.key}:${it.value}")
	}
  }
  internal fun connectSync(): HTTPConnectResult {
	if (verbose) printInfo()
	val result = openConnection()
	if (verbose) {
	  if (result is HTTPResponse) {
		println("Response = ${result.statusCode}")
		println("Data = ${result.text}")
	  } else {
		println("Result = $result")
	  }
	}
	return result
  }
  internal fun connectAsync(): HTTPAsyncConnection {
	if (verbose) printInfo()
	return openAsyncConnection()
  }
  abstract fun configureForWritingBytes(bytes: ByteArray)
  fun configureForWritingString(string: String) = configureForWritingBytes(string.encodeToByteArray())
  inline fun <reified T> configureForWritingJson(someData: T) {
	headers {
	  contentType = applicationJsonCharsetUTF8
	}
	configureForWritingString(Json.encodeToString(someData))
  }
}

expect class HTTPRequestImpl internal constructor(url: FileOrURL): HTTPRequest {
  var timeout: Duration?
  override var method: HTTPMethod
  override fun setRequestProperty(name: String, value: String?)
  override fun allRequestHeaders(): Map<String, List<String>>
  override fun openConnection(): HTTPConnectResult
}


