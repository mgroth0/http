package matt.http.url.query

import io.ktor.http.*
import matt.collect.map.filterOutNullValues
import matt.http.url.MURL
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.lang.inList
import matt.model.op.convert.BooleanStringConverter
import matt.model.op.convert.DefiniteIntStringConverter
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
infix fun MURL.withQueryParams(params: Map<String, String>): MURL {
    return buildQueryURL(cpath, params)
}

@JvmName("buildQueryURL1")
fun buildQueryURL(
    mainURL: String,
    vararg params: Pair<String, String>
) = buildQueryURL(mainURL, params.toMap())

@JvmName("buildQueryURL2")
fun buildQueryURL(
    mainURL: String,
    params: Map<String, String>
): MURL {
    return buildQueryURL(mainURL, params.entries.associate { it.key to it.value.inList() })
}

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


fun MURL.withPort(port: Int): MURL {
    if (":" in cpath) {
        error("not ready if already has port")
    }
    return MURL(cpath.substringBefore("/") + ":" + port.toString() + "/" + cpath.substringAfter("/"))
}


private const val PROTOCOL_DELIMITER = "://"

fun MURL.withProtocol(protocol: URLProtocol): MURL {
    return if (PROTOCOL_DELIMITER in cpath) {
        MURL(protocol.name + PROTOCOL_DELIMITER + cpath.substringAfter(PROTOCOL_DELIMITER))
    } else {
        MURL(protocol.name + PROTOCOL_DELIMITER + cpath)
    }
}


abstract class QueryParams : Query {

    override val params = mutableListOf<Param<*>>()

    open inner class Param<T : Any>(
        override val name: String,
        converter: StringListConverter<T>
    ) : QueryParam {
        private val realConverter = converter.emptyIsNull()
        override var value: StringList = emptyList()
        val convertedValue get() = realConverter.fromStringList(value)
        fun setFrom(newValue: T?) {
            value = realConverter.toStringList(newValue)
        }

        override fun toString(): String {
            return "${this::class.simpleName}[name=$name,size=${value.size},value=${value.elementsToString()}]"
        }
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

    fun <T : Any> singleParam(converter: StringConverter<T>) =
        param(StringListConverter.fromStringConverterAsSingular(converter))


    override fun toMap() = params.associate { it.name to it.value }.filterOutNullValues()

    override fun urlStringRep(): String {
        return buildQueryURL("", toMap()).cpath
    }

    override fun toString(): String {
        return urlStringRep()
    }


}

