@file:JvmName("ConnectionJvmKt")

package matt.http.connection

import matt.lang.function.Consume
import java.io.InputStream
import java.net.http.HttpResponse
import kotlin.concurrent.thread


typealias JavaHTTPRequest = java.net.http.HttpRequest

/*JavaHTTPRequest*/
class JHTTPConnection internal constructor(jCon: HttpResponse<InputStream>): HTTPResponse {


  override val statusCode = jCon.statusCode().toShort()

  private var onlyOne = 0
  val inputStream = jCon.body()
	@Synchronized get() {
	  onlyOne++
	  require(onlyOne <= 1)
	  return field
	}


  /*  @Synchronized
	fun stream(): InputStream {
	  onlyOne++
	  require(onlyOne<=1)
	  return jCon.body()
	}*/

  private val fullRead by lazy {
	jCon.body().readAllBytes()
  }

  @Synchronized
  fun full(): ByteArray {
	/*onlyOne++
	require(onlyOne<=1)*/
	return fullRead
  }

  /*fun checkForErrorMessages() {*/

  /*warn("is this an error? ${statusCode},${fullRead.decodeToString()}")*/
  /*if (jCon.statusCode() > 300) {
	err("is this an error?")
  }*/

  /*	jCon.errorStream?.readAllBytes()?.let {

		*//*
	  *
	  *   * <p>This method will not cause a connection to be initiated.  If
	  * the connection was not connected, or if the server did not have
	  * an error while connecting or if the server had an error but
	  * no error data was sent, this method will return null. This is
	  * the default.
	  *
	  * @return an error stream if any, null if there have been no
	  * errors, the connection is not connected or the server sent no
	  * useful data.
	  * *//*

	  println("ERR: ${it.decodeToString()}")
	} ?: run {
	  *//*if (verbose) {*//*
	  println("no error message from server")
	  *//*}*//*
	}*/

  /*}*/

  /*  val inputStream: InputStream
	  get() {
		try {
		  return jCon.inputStream
		} catch (e: Exception) {
		  checkForErrorMessages()
		  throw e
		}
	  }*/


  /*
	override val statusCode: Int
	  get() {
		try {
		  return jCon.responseCode
		} catch (e: Exception) {
		  checkForErrorMessages()
		  throw e
		}
	  }
  */


  /*  var timeout: Duration?
	  get() = jCon.readTimeout.takeIf { it != 0 }?.let {
		require(it > 0)
		it.milliseconds
	  }
	  set(value) {
		jCon.readTimeout = value?.inWholeMilliseconds?.toInt() ?: 0
	  }*/

  override val bytes: ByteArray
	/*get() = inputStream.readAllBytes()*/
	get() = fullRead

  override val text by lazy {
	/*inputStream.bufferedReader().readText()*/
	fullRead.decodeToString()
  }

  override fun toString(): String {
	return "JHTTPConnection[status=${statusCode}]"
  }

}



class JHTTPAsyncConnection(private val resultGetter: ()->HTTPConnectResult): HTTPAsyncConnection {

  override fun whenDone(op: Consume<HTTPConnectResult>) {
	/*obviously this can be done way more efficiently*/
	thread {
	  op(resultGetter())
	}
  }

}