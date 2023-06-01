package matt.http.url.req

import kotlinx.coroutines.test.runTest
import matt.http.http
import matt.http.lib.RequiresCookiesException
import matt.http.url.valid.ValidURLTests.Companion.GOOGLE
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class HttpRequests {

    companion object {
        internal const val TWITTER = "https://www.twitter.com/"
    }

    @Test
    fun twitter() = runTest {
        assertThrows<RequiresCookiesException> {
            http(TWITTER)
        }
    }

    @Test
    fun google() = runTest {
        http(GOOGLE)
    }


}