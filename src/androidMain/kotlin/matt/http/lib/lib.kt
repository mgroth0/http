
package matt.http.lib

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual val httpClientEngine: HttpClientEngine by lazy {

    /*

//    DONT USE THIS! It gets the job done, yes. However, it is old and intended only for android apps that target older Android versions. It is hard coded to use HTTP Version 1 (causing a test for this I just made to fail!)


//     this also does not support websockets!


//    see: https://ktor.io/docs/http-client-engines.html#limitations


    Android.create {

    }

     */





    OkHttp.create {
    }
}


