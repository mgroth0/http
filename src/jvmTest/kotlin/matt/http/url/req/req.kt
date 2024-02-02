package matt.http.url.req

import matt.http.http
import matt.http.lib.RequiresCookiesException
import matt.http.url.valid.ValidURLTests
import matt.test.co.runTestWithTimeoutOnlyIfTestingPerformance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import kotlin.test.Test

@Execution(CONCURRENT)
class HttpRequests {

    companion object {
        internal const val TWITTER = "https://www.twitter.com/"
    }

    @Test
    fun twitter() = runTestWithTimeoutOnlyIfTestingPerformance {
        assertThrows<RequiresCookiesException> {
            http(TWITTER)
        }
    }

    @Test
    fun google() = runTestWithTimeoutOnlyIfTestingPerformance {
        http(ValidURLTests.GOOGLE)
    }


}
