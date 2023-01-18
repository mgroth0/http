package matt.http.internet

import org.w3c.xhr.XMLHttpRequest


actual fun TheInternet.isAvailable(): Boolean {
  val req = XMLHttpRequest()
  req.open("GET", "https://www.google.com/")
  require(req.status == 200.toShort()) {
	"detecting offline in JS is not implemented yet"
  }
  return true
}