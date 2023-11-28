package matt.http.duri

import matt.lang.mime.MimeData
import matt.prim.base64.encodeToBase64


fun MimeData.toDataUri() = constructDataUri(mimeType = mimeType, data = data)

private fun constructDataUri(
    mimeType: String,
    data: String
) = "data:$mimeType;base64,${data.encodeToBase64()}"

