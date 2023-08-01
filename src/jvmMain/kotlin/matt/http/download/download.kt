package matt.http.download

import matt.file.MFile
import matt.lang.err
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

fun java.net.URL.download(file: MFile) {
    if (file.exists()) {
        err("do I want to overwrite?")
    }
    file.parentFile!!.mkdirs()


    val inputStream: InputStream = openStream()
    val outputStream: OutputStream = FileOutputStream(file)
    val buffer = ByteArray(2048)

    var length: Int

    while (inputStream.read(buffer).also { length = it } != -1) {
        println("Buffer Read of length: $length")
        outputStream.write(
            buffer,
            0,
            length
        )
    }

    inputStream.close()
    outputStream.close()
}
