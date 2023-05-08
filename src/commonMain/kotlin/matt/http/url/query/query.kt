package matt.http.url.query

import matt.http.url.MURL

infix fun MURL.query(params: Map<String, String>) = withQueryParams(params)
infix fun MURL.withQueryParams(params: Map<String, String>): MURL {
    return MURL(buildQueryURL(cpath, params))
}

fun buildQueryURL(mainURL: String, vararg params: Pair<String, String>) = buildQueryURL(mainURL, params.toMap())
fun buildQueryURL(mainURL: String, params: Map<String, String>): String {
    return "$mainURL${
        params.entries.joinToString(
            separator = "&",
            prefix = if ("?" in mainURL) "&" else "?"
        ) { "${it.key}=${it.value}" }
    }"
}


fun MURL.withPort(port: Int): MURL {
    if (":" in cpath) {
        error("not ready if already has port")
    }
    return MURL(cpath.substringBefore("/") + ":" + port.toString() + "/" + cpath.substringAfter("/"))
}