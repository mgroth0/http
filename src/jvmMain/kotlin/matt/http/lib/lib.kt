@file:JvmName("LibJvmKt")

package matt.http.lib

import io.ktor.client.engine.*
import io.ktor.client.engine.java.*


actual val httpClientEngine: HttpClientEngine by lazy {
  Java.create {

  }
}