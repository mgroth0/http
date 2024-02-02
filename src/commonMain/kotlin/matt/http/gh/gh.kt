package matt.http.gh

import matt.prim.str.removePrefixAndOrSuffix

/*todo: put this in a more appropriate module*/

fun rawGithubURL(
    user: String,
    repo: String,
    branch: String,
    path: String
) = listOf(
    "https://raw.githubusercontent.com",
    user,
    repo,
    branch,
    path
).joinToString(separator = "/") { it.removePrefixAndOrSuffix("/") }
