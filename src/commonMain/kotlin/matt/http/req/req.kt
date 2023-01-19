package matt.http.req

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.file.FileOrURL
import matt.http.connection.HTTPAsyncConnection
import matt.http.connection.HTTPConnectResult
import matt.http.connection.HTTPResponse
import matt.http.headers.HTTPHeaders
import matt.http.method.HTTPMethod
import matt.lang.ILLEGAL
import matt.lang.anno.SeeURL
import matt.log.tab
import kotlin.time.Duration

@SeeURL("https://youtrack.jetbrains.com/issue/KT-20427") abstract class HTTPRequest {

  abstract protected val url: FileOrURL


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
		println("Message = ${result.text}")
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

  //  internal abstract fun writeBytesNow(bytes: ByteArray)
  abstract fun configureForWritingBytes(bytes: ByteArray)

  //  fun writeStringNow(string: String) = writeBytesNow(string.encodeToByteArray())
  fun configureForWritingString(string: String) = configureForWritingBytes(string.encodeToByteArray())


  //  inline fun <reified T> configureForwriteAsJsonNow(someData: T) {
  //	writeStringNow(Json.encodeToString(someData))
  //  }

  inline fun <reified T> configureForWritingJson(someData: T) {
	headers {
	  contentType = "application/json;charset=UTF-8"
	}
	configureForWritingString(Json.encodeToString(someData))

  }


}

expect class HTTPRequestImpl internal constructor(url: FileOrURL): HTTPRequest {
  var timeout: Duration?

  /*method = PUT
  writeAsync(tarFile)
  verbose = true*/
  override var method: HTTPMethod

//  override fun getRequestProperty(name: String): String?
  override fun setRequestProperty(name: String, value: String?)
  override fun allRequestHeaders(): Map<String, List<String>>


  //  internal override fun writeBytesNow(bytes: ByteArray)

  override fun openConnection(): HTTPConnectResult
}

var HTTPRequestImpl.data: ByteArray
  get() = ILLEGAL
  set(value) {
	configureForWritingBytes(value)
  }

//var HTTPRequestImpl.jsonData: ByteArray
//  get() = ILLEGAL
//  set(value) {
//	writer = BasicHTTPWriter(this, value)
//  }


fun HTTPRequest.headers(op: HTTPHeaders.()->Unit) {
  HTTPHeaders(this).apply(op)
}