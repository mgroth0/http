package matt.http.url.query

import io.ktor.http.URLProtocol
import matt.collect.map.filterOutNullValues
import matt.http.url.MURL
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.lang.inList
import matt.model.op.convert.BooleanStringConverter
import matt.model.op.convert.DefiniteIntStringConverter
import matt.model.op.convert.EnumNameStringConverter
import matt.model.op.convert.StringStringConverter
import matt.osi.params.RawParams
import matt.osi.params.query.Query
import matt.osi.params.query.QueryParam
import matt.prim.converters.StringConverter
import matt.prim.converters.StringList
import matt.prim.converters.StringListConverter
import matt.prim.converters.StringListStringListConverter
import matt.prim.str.elementsToString
import matt.prim.str.mybuild.api.string
import kotlin.enums.enumEntries
import kotlin.jvm.JvmName

@JvmName("query1")
infix fun MURL.query(params: QueryParams): MURL = query(params.toMap())

@JvmName("query2")
infix fun MURL.query(params: RawParams) = withQueryParams(params)

@JvmName("withQueryParams1")
infix fun MURL.withQueryParams(params: QueryParams): MURL = query(params.toMap())

@JvmName("withQueryParams2")
infix fun MURL.withQueryParams(params: RawParams): MURL = query(params.toMap())

@JvmName("withQueryParams3")
infix fun MURL.withQueryParams(params: Map<String, String>): MURL = buildQueryURL(path, params)

@JvmName("buildQueryURL1")
fun buildQueryURL(
    mainURL: String,
    vararg params: Pair<String, String>
) = buildQueryURL(mainURL, params.toMap())

@JvmName("buildQueryURL2")
fun buildQueryURL(
    mainURL: String,
    params: Map<String, String>
): MURL = buildQueryURL(mainURL, params.entries.associate { it.key to it.value.inList() })

@JvmName("buildQueryURL3")
fun buildQueryURL(
    mainURL: String,
    params: Map<String, List<String>>
): MURL {

    val rawString = string {
        append(mainURL)
        if (params.isNotEmpty()) {
            if ("?" !in mainURL) {
                append("?")
            } else {
                append("&")
            }
            append(
                params.entries.flatMap { (k, v) -> v.map { "$k=$it" } }.joinToString(separator = "&")
            )
        }
    }

    return MURL(rawString)
}


fun MURL.withFragment(fragment: String) = buildFragmentURL(path,fragment)

fun buildFragmentURL(
    mainURL: String,
    fragment: String
): MURL {
    check("#" !in mainURL)
    check("#" !in fragment)
    return MURL("$mainURL#$fragment")
}


fun MURL.withPort(port: Int): MURL {
    if (":" in path) {
        error("not ready if already has port")
    }
    return MURL(path.substringBefore("/") + ":" + port.toString() + "/" + path.substringAfter("/"))
}


private const val PROTOCOL_DELIMITER = "://"

fun MURL.withProtocol(protocol: URLProtocol): MURL = if (PROTOCOL_DELIMITER in path) {
    MURL(protocol.name + PROTOCOL_DELIMITER + path.substringAfter(PROTOCOL_DELIMITER))
} else {
    MURL(protocol.name + PROTOCOL_DELIMITER + path)
}


abstract class QueryParams : Query {

    final override val params = mutableListOf<Param<*>>()

    open inner class Param<T : Any>(
        final override val name: String,
        converter: StringListConverter<T>
    ) : QueryParam {
        private val realConverter = converter.emptyIsNull()
        final override var value: StringList = emptyList()
        val convertedValue get() = realConverter.fromStringList(value)
        fun setFrom(newValue: T?) {
            value = realConverter.toStringList(newValue)
        }

        final override fun toString(): String = "${this::class.simpleName}[name=$name,size=${value.size},value=${value.elementsToString()}]"
    }


    fun stringListParam() = param(StringListStringListConverter)
    fun <T : Any> param(converter: StringListConverter<T>) = provider {
        val p = Param(it, converter = converter)
        params += p
        varProp(getter = { p.convertedValue }, setter = { p.setFrom(it) })
    }

    fun stringParam() = singleParam(StringStringConverter)

    fun boolParam() = singleParam(BooleanStringConverter)

    fun intParam() = singleParam(DefiniteIntStringConverter)


    inline fun <reified E : Enum<E>> enumParam() = singleParam(EnumNameStringConverter(enumEntries<E>()))

    fun <T : Any> singleParam(converter: StringConverter<T>) =
        param(StringListConverter.fromStringConverterAsSingular(converter))


    final override fun toMap() = params.associate { it.name to it.value }.filterOutNullValues()

    final override fun urlStringRep(): String = buildQueryURL("", toMap()).path

    final override fun toString(): String = urlStringRep()


}

