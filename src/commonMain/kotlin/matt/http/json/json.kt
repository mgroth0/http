package matt.http.json

import matt.http.connection.HTTPConnectResult
import matt.http.connection.requireSuccessful
import matt.json.parse
import matt.json.prim.IgnoreUnknownKeysJson

/*this shouldn't be here. Bad module placement, but I'm in a rush for good reasons*/
suspend inline fun <reified T : Any> HTTPConnectResult.requireIs() =
    requireSuccessful().text().parse<T>()

suspend inline fun <reified T : Any> HTTPConnectResult.requireIsPartially() =
    requireSuccessful().text().parse<T>(IgnoreUnknownKeysJson)