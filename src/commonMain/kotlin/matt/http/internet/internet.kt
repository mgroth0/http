package matt.http.internet

import matt.http.url.MURL
import matt.lang.not
import matt.model.code.valjson.ValJson.Port

class TheInternet {
  //  val wasNotAvailableInThisRuntime get() = not(wasAvailableInThisRuntime)

  //  fun isAvailable(): Boolean
  /*ugh, won't work with gradle daemon. There has to be a way to set properties that are reset for the gradle daemon*/
  val wasAvailableInThisRuntime by lazy {
	isAvailable()
  }
  val wasNotAvailableInThisRuntime get() = not(wasAvailableInThisRuntime)

}

expect fun TheInternet.isAvailable(): Boolean

fun TheInternet.isNotAvailable() = not(isAvailable())


/*not https*/
val LOCAL_TEST_URL = MURL("http://0.0.0.0:${Port.localKtorTest}")