package matt.http.internet

import matt.http.url.MURL
import matt.lang.not

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
fun LOCAL_TEST_URL(port: Int) = MURL("http://0.0.0.0:${port}")