package matt.http.headers.base

import matt.http.HTTPDslMarker
import matt.http.req.MutableHeaders
import matt.http.req.valueForHeader
import matt.lang.delegation.provider
import matt.lang.delegation.varProp
import matt.model.op.convert.StringStringConverter
import matt.prim.converters.StringConverter


@HTTPDslMarker
abstract class HTTPHeadersBase internal constructor(
    private val con: MutableHeaders
) {

    protected fun <T> addHeader(
        key: String,
        value: T,
        converter: StringConverter<T>
    ) {
        val oldValueConverted = validateHeaderIsAbsentOrHasValue(key, value, converter)
        if (oldValueConverted != value) {
            con.addHeader(key, converter.toString(value))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    operator fun set(
        s: String,
        value: String
    ) {
        error("unclear how to set or if I should set now that I understand that its not a map")
    }


    private fun <T> validateHeaderIsAbsentOrHasValue(
        header: String,
        value: T,
        converter: StringConverter<T>
    ): T? {
        val oldValue = con.valueForHeader(header)
        val oldValueConverted = oldValue?.let { converter.fromString(it) }
        require(oldValue == null || oldValueConverted == value) {
            "unclear if I am adding or setting here (oldValue=$oldValue,newValue=$value)"
        }
        return oldValueConverted
    }


    private fun addHeader(
        key: String,
        value: String
    ) = addHeader(key, value, StringStringConverter)

    /*only commenting this out because it is unused and I'm dealing with JDK 1.8 inline issues*/
    /*private fun propProvider(key: String) = provider {
      varProp(
        getter = { con.valueForHeader(key) },
        setter = {
          require(it != null) {
            "not sure how to handle this yet"
          }
          addHeader(key, it)
        }
      )
    }*/

    protected fun <T> propProvider(
        key: String,
        converter: StringConverter<T & Any>
    ) = provider {
        varProp(
            getter = {
                val s = con.valueForHeader(key)
                s?.let { converter.fromString(s) }
            },
            setter = {
                requireNotNull(it) {
                    "not sure how to handle this yet"
                }
                addHeader(key, it, converter)
            }
        )
    }

}
