package matt.http.rest

import matt.model.rest.RestResource

object Ping : RestResource<String> {
    override val path = "ping"
}