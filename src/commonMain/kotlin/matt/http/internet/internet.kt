package matt.http.internet

import matt.http.url.MURL
import matt.lang.not
import matt.model.code.valjson.ValJson.Port

class TheInternet {
//    val wasAvailableInThisRuntime by lazy {
//        isAvailable()
//    }
//    val wasNotAvailableInThisRuntime get() = not(wasAvailableInThisRuntime)


    private var gotAReport = false
    fun reportUnavailability() {
        gotAReport = true
    }

    val wasAlwaysAvailableInThisRuntime get() = !gotAReport
    val wasUnavilableThisRuntime get() = gotAReport


}

expect fun TheInternet.isAvailable(): Boolean
fun TheInternet.isNotAvailable() = not(isAvailable())


/*not https*/
val LOCAL_TEST_URL = MURL("http://0.0.0.0:${Port.localKtorTest}")