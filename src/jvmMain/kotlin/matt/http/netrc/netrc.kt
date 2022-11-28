package matt.http.netrc

import matt.file.commons.USER_HOME

object NetRC {
  private val netrc = USER_HOME[".netrc"]
  private val lines = netrc.readText().lines().map { it.trim() }
  val login = lines.first { it.startsWith("login") }.substringAfter("login").trim()
  val password = lines.first { it.startsWith("password") }.substringAfter("password").trim()
}

