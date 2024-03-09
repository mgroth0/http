package matt.http.sse.test.sse

import matt.http.sse.Comment
import matt.http.sse.JustData
import matt.http.sse.ServerSideEventsClient
import kotlin.test.Test


class ServerSideEventTests() {
    @Test
    fun instantiateClasses() {
        Comment("")
        JustData("")
        ServerSideEventsClient()
    }
}
