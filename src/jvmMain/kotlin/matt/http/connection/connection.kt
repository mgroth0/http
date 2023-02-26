@file:JvmName("ConnectionJvmKt")

package matt.http.connection

import java.io.InputStream
import java.net.http.HttpResponse


/*JavaHTTPRequest*/
class JHTTPConnection internal constructor(jCon: HttpResponse<InputStream>): HTTPConnection {


  private var onlyOne = 0
  val inputStream = jCon.body()
	@Synchronized get() {
	  onlyOne++
	  require(onlyOne <= 1)
	  return field
	}


  /*  @Synchronized
	fun stream(): InputStream {
	  onlyOne++
	  require(onlyOne<=1)
	  return jCon.body()
	}*/
}



