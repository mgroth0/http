package matt.http.download

import matt.lang.err
import matt.lang.file.toJFile
import matt.lang.model.file.AnyFsFile
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

fun java.net.URL.download(file: AnyFsFile) {
    if (file.toJFile().exists()) {
        err("do I want to overwrite?")
    }
    file.parent!!.toJFile().mkdirs()


    val inputStream: InputStream = openStream()
    val outputStream: OutputStream = FileOutputStream(file.toJFile())
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
