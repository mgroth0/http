package matt.http.url.ser

import matt.http.url.MURL
import matt.model.ser.EncodedAsStringSerializer


object UrlSerializer: EncodedAsStringSerializer<MURL>() {
    override fun MURL.encodeToString(): String = path

    override fun String.decode(): MURL = MURL(this)
}
