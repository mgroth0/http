package matt.http.headers.key

import matt.http.headers.auth.AuthHeader
import matt.http.headers.auth.BearerConverter
import matt.http.headers.content.HTTPContentTypeConverter
import matt.http.headers.content.HTTPMediaType
import matt.model.data.hash.md5.MD5
import matt.model.data.hash.md5.MD5Converter
import matt.model.op.convert.StringStringConverter
import kotlin.jvm.JvmInline

interface HttpHeaderName<T> {
    val name: String /*case insensitive*/
    fun valueToString(value: T): String
}

data object Accept: HttpHeaderName<HTTPMediaType> {
    override val name = "Accept"
    private val converter = HTTPContentTypeConverter
    override fun valueToString(value: HTTPMediaType) = HTTPContentTypeConverter.toString(value)
}


object ContentType: HttpHeaderName<HTTPMediaType> {
    override val name = "Content-Type"
    private val converter = HTTPContentTypeConverter
    override fun valueToString(value: HTTPMediaType) = HTTPContentTypeConverter.toString(value)
}

object Auth: HttpHeaderName<AuthHeader> {
    override val name = "Authorization"
    private val converter = BearerConverter
    override fun valueToString(value: AuthHeader) = BearerConverter.toString(value)
}


object MattMd5: HttpHeaderName<MD5> {
    override val name = "matt-md5"
    private val converter = MD5Converter
    override fun valueToString(value: MD5) = MD5Converter.toString(value)
}

@JvmInline
value class MySpecialBearerAuth(val subName: String): HttpHeaderName<AuthHeader> {
    override val name get() = "Authorization-$subName"
    private val converter get() = BearerConverter
    override fun valueToString(value: AuthHeader) = BearerConverter.toString(value)
}

object XGitHubApiVersion: HttpHeaderName<String> {
    override val name = "X-GitHub-Api-Version"
    private val converter = StringStringConverter
    override fun valueToString(value: String) = value
}
