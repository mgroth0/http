@file:JvmName("LibAndroidKt")

package matt.http.lib

import io.ktor.client.engine.*
import io.ktor.client.engine.android.*

actual val httpClientEngine: HttpClientEngine by lazy {
    Android.create {

    }
}


