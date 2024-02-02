package matt.http

import io.ktor.http.HttpProtocolVersion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals


class CommonHttpTests() {
    @Test
    fun clientUsesHttp2() {
        runTest {
            val response = http("https://www.google.com")
            assertEquals(HttpProtocolVersion.HTTP_2_0, response.protocol())
        }
    }
}
