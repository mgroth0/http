package matt.http.url.query

import matt.collect.map.filterOutNullValues
import matt.http.url.MURL
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.model.rest.Query


infix fun MURL.query(params: QueryParams): MURL = query(params.toMap())

infix fun MURL.query(params: Map<String, String>) = withQueryParams(params)
infix fun MURL.withQueryParams(params: QueryParams): MURL = query(params.toMap())
infix fun MURL.withQueryParams(params: Map<String, String>): MURL {
    return buildQueryURL(cpath, params)
}

fun buildQueryURL(
    mainURL: String,
    vararg params: Pair<String, String>
) = buildQueryURL(mainURL, params.toMap())

fun buildQueryURL(
    mainURL: String,
    params: Map<String, String>
): MURL {
    return MURL("$mainURL${
        params.entries.joinToString(
            separator = "&",
            prefix = if ("?" in mainURL) "&" else "?"
        ) { "${it.key}=${it.value}" }
    }")
}


fun MURL.withPort(port: Int): MURL {
    if (":" in cpath) {
        error("not ready if already has port")
    }
    return MURL(cpath.substringBefore("/") + ":" + port.toString() + "/" + cpath.substringAfter("/"))
}


abstract class QueryParams : Query {

    val params = mutableListOf<Param>()

    inner class Param(val name: String) {
        var value: String? = null
    }

    fun param() = provider {
        val p = Param(it)
        params += p
        varProp(
            getter = { p.value },
            setter = { p.value = it }
        )
    }


    override fun toMap() = params.associate { it.name to it.value }.filterOutNullValues()

    override fun urlStringRep(): String {
        return buildQueryURL("", toMap()).cpath
    }

    override fun toString(): String {
        return urlStringRep()
    }


}