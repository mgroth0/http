package matt.http.s3

import matt.http.url.MURL

fun rawS3Url(
    bucket: String,
    path: String,
    region: String? = null
): MURL = MURL("https://$bucket.s3${region?.let { ".$it" } ?: ""}.amazonaws.com/$path")
