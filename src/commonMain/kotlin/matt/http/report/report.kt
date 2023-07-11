package matt.http.report

import io.ktor.http.*
import io.ktor.util.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.model.code.errreport.CommonThrowReport
import matt.model.code.errreport.Report
import matt.prim.str.elementsToString
import matt.prim.str.mybuild.string
import kotlin.collections.Map.Entry

enum class EndSide {
    Client, Server;

    val sideLabel get() = name + "Side"
}


class HTTPRequestReport(
    side: EndSide,
    date: LocalDateTime,
    throwReport: CommonThrowReport,
    attributes: List<AttributeKey<*>>,
    parameters: Map<String, List<String>>,
    uri: String,
    method: String,
    headers: Map<String, List<String>>,
    /*payload: String? = null*/
) : Report() {


    companion object {
        val json = Json {
            prettyPrint = true
        }
    }

    override val text by lazy {
        string {
            lineDelimited {
                +"${side.sideLabel} HTTP Request Report"
                blankLine()
                +"DATE: $date"
                +"URI: $uri"
                +"METHOD: $method"
                +"HEADERS: ${json.encodeToString(headers)}"
                +"Attributes: ${attributes.elementsToString()}"
                +"Parameters: ${json.encodeToString(parameters)}"
                blankLine()
                /*+"Payload: $payload"*/
                +"Throw Report:"
                +throwReport
            }
        }
    }
}

class HTTPResponseReport(
    status: HttpStatusCode,
    headers: List<Entry<String, List<String>>>,
    body: String
) : Report() {


    companion object {
        val json = Json {
            prettyPrint = true
        }
    }

    override val text by lazy {
        string {
            lineDelimited {
                +"HTTP Response Report"
                blankLine()
                +"STATUS: $status"
                +"HEADERS: ${json.encodeToString(headers)}"
                +"BODY: $body"
            }
        }
    }
}