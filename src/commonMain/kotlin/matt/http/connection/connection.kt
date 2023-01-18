package matt.http.connection

interface HTTPConnection {
  fun getRequestProperty(name: String): String?
  fun setRequestProperty(name: String, value: String?)
}