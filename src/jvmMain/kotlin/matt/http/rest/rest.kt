package matt.http.rest

import io.ktor.http.*
import matt.file.hash.md5
import matt.http.api.API
import matt.http.headers.headers
import matt.http.json.requireIs
import matt.http.method.HTTPMethod.DELETE
import matt.http.method.HTTPMethod.POST
import matt.http.method.HTTPMethod.PUT
import matt.model.rest.BaseRestResource
import matt.model.rest.NoArg
import matt.model.rest.NoQuery
import matt.model.rest.Query
import matt.model.rest.RestResource
import matt.model.rest.RestResourceWithArgument
import matt.model.rest.RestResourceWithLocation
import matt.model.rest.RestResourceWithMultiPartArgument
import matt.model.rest.RestResourceWithQuery
import matt.model.rest.concretePath

context(API)
suspend inline fun <reified R : Any> RestResource<R>.get() = http(path).requireIs<R>()

context(API)
suspend inline fun <reified R : Any, reified Q : Query> RestResourceWithQuery<R, Q>.get(query: Q) =
    http(concretePath(NoArg, query)).requireIs<R>()

context(API)
suspend inline fun <reified R : Any, reified A> RestResourceWithArgument<R, A>.get(arg: A) =
    http(concretePath(arg)).requireIs<R>()

context(API)
suspend inline fun <reified R : Any, reified L : Any> RestResourceWithLocation<R, L>.post(): L {
    val resp = http(concretePostPath()) {
        method = POST
    }
    resp.requireSuccessful()
    require(resp.statusCode() == HttpStatusCode.Created) {
        "got status code ${resp.statusCode()}, text = ${resp.text()}"
    }
    return resp.requireIs<L>()
}

context(API)
suspend inline fun <reified R : Any> RestResource<R>.put(r: R) {
    put(r, NoArg)
}

context(API)
suspend inline fun <reified R : Any, A, T> BaseRestResource<R, A, T, NoQuery>.put(
    r: R,
    arg: A
) {
    val resp = http(concretePath(arg, NoQuery)) {
        method = PUT
        when (r) {
            is String -> configureForWritingString(r)
            else      -> configureForWritingJson(r)
        }

    }
    resp.requireSuccessful()
    require(resp.statusCode() == HttpStatusCode.OK) {
        "got status code ${resp.statusCode()}, text = ${resp.text()}"
    }
}

context(API)
suspend inline fun <reified R : Any, A> RestResourceWithMultiPartArgument<R, A>.upload(
    data: ByteArray,
    arg: A
) {
    val resp = http(concretePath(arg, NoQuery)) {
        method = PUT
        headers {
            md5 = data.md5()
        }
        configureForWritingBytes(data)
    }
    resp.requireSuccessful()
    require(resp.statusCode() == HttpStatusCode.OK) {
        "got status code ${resp.statusCode()}, text = ${resp.text()}"
    }
}


context(API)
suspend inline fun <reified R : Any> RestResource<R>.delete() {
    delete(NoArg)
}

context(API)
suspend inline fun <reified R : Any, A, T> BaseRestResource<R, A, T, NoQuery>.delete(
    arg: A
) {
    val resp = http(concretePath(arg, NoQuery)) {
        method = DELETE
    }
    resp.requireSuccessful()
    require(resp.statusCode() == HttpStatusCode.NoContent) {
        "got status code ${resp.statusCode()}, text = ${resp.text()}"
    }
}



