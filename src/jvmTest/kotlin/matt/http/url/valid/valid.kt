package matt.http.url.valid

import matt.prim.str.remove
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class ValidURLTests {

    companion object {
        internal const val GOOGLE = "https://www.google.com/"

    }

    @Test
    fun httpIsValid() {
        ValidatingHTTPURL(GOOGLE.replace("https", "http"))
    }

    @Test
    fun httpsIsValid() {
        ValidatingHTTPURL(GOOGLE)
    }

    @Test
    fun noHttpIsInValid() {
        assertThrows<MalformedURLException> {
            ValidatingHTTPURL(GOOGLE.remove("https://"))
        }
    }

    @Suppress("ConvertToStringTemplate")
    @Test
    fun spaceAfterIsInValid() {
        assertThrows<MalformedURLException> {
            ValidatingHTTPURL(GOOGLE + ' ')
        }
    }

    @Suppress("ConvertToStringTemplate")
    @Test
    fun spaceBeforeIsInValid() {
        assertThrows<MalformedURLException> {
            ValidatingHTTPURL(' ' + GOOGLE)
        }
    }

    @Test
    fun spaceWithinIsInValid() {
        assertThrows<MalformedURLException> {
            ValidatingHTTPURL(GOOGLE.replace("g", "g" + ' '))
        }
    }


    @Test
    fun paraenthesesAreValid() {
        ValidatingHTTPURL("https://www.wikiwand.com/en/Shim_(computing)")
    }

    @Test
    fun hashesAreValid() {
        ValidatingHTTPURL("https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/topics/debugging.md#stacktrace-recovery")
    }


    @Test
    fun googleIsValid() {
        ValidatingHTTPURL(GOOGLE)
    }




}