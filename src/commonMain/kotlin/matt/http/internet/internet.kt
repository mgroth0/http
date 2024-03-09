package matt.http.internet

import matt.http.http
import matt.http.url.MURL
import matt.lang.common.not

class TheInternet {


    private var gotAReport = false
    fun reportUnavailability() {
        gotAReport = true
    }

    val wasAlwaysAvailableInThisRuntime get() = !gotAReport
    val wasUnavilableThisRuntime get() = gotAReport
}

suspend fun TheInternet.isAvailable(): Boolean {
    http("https://www.google.com").requireSuccessful()
    /*now that I rewrote this in common, I have to rewrite the exception handlers to return false when appropriate!*/
    return true
}

suspend fun TheInternet.isNotAvailable() = not(isAvailable())


/*not https*/
fun LOCAL_TEST_URL(port: Int) = MURL("http://0.0.0.0:$port")
