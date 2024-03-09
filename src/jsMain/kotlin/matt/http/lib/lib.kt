package matt.http.lib

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js


actual val httpClientEngine: HttpClientEngine by lazy {
    Js.create {
    }
}

