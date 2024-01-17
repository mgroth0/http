@file:JvmName("LibJvmKt")

package matt.http.lib

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java
import java.net.http.HttpClient.Version.HTTP_2


actual val httpClientEngine: HttpClientEngine by lazy {
    Java.create {
        /*the default was http 1.1 and I think this was making heroku angry?*/
        this.protocolVersion = HTTP_2
    }
}