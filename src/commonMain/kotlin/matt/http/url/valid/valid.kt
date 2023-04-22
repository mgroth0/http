package matt.http.url.valid

import matt.lang.anno.SeeURL
import matt.lang.anno.SupportedByChatGPT
import matt.prim.str.truncateWithElipses
import kotlin.jvm.JvmInline

fun String.isValidHttpUrl(): Boolean {

    return try {
        ValidatingHTTPURL(this)
        true
    } catch (e: MalformedURLException) {
        false
    }

}

fun String.tryValidatingURL(): ValidHTTPURL? {
    return try {
        ValidatingHTTPURL(this)
    } catch (e: MalformedURLException) {
        null
    }
}


interface ValidURI

interface ValidURL : ValidURI

interface ValidHTTPURL : ValidURL {
    val url: String
}


@JvmInline
value class ValidatingHTTPURL(override val url: String) : ValidHTTPURL {
    companion object {

        @SeeURL("https://www.wikiwand.com/en/Shim_(computing)")
        @SeeURL("https://stackoverflow.com/questions/13225028/is-it-ok-to-have-brackets-or-parenthesis-in-url#:~:text=Parentheses%20%E2%80%9C()%E2%80%9D%20may%20be%20used,%E2%80%9D%20and%20%E2%80%9C%29%E2%80%9D.")
        @SeeURL("https://webmasters.stackexchange.com/questions/78110/is-it-bad-to-use-parentheses-in-a-url")
        /*original regex by ChatGPT did not allow parenthesis*/
        private const val ALLOW_PARENTHESIS = true

        @SupportedByChatGPT
        private val REGEX = Regex(
            pattern =
            """^https?://(?:[-\w.]|%[\da-fA-F]{2})+/(?:[-\w./${if (ALLOW_PARENTHESIS) "()" else ""}?%&=]*)?$"""
        )
    }

    init {
        if (!REGEX.matches(url)) {
            throw MalformedURLException(url)
        }
    }
}

class MalformedURLException(malformedURL: String) :
    Exception("${malformedURL.truncateWithElipses(100)} is not a valid url")



