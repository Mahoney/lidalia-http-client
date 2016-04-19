package uk.org.lidalia
package http.client.async

import java.io.{ByteArrayOutputStream, InputStream}

class CapturingInputStream(decorated: InputStream, maxSize: Int = 1024 * 512) extends InputStream {

  val captured = new ByteArrayOutputStream(maxSize)

  override def read(): Int = {
    doRead()
  }

  private def doRead(): Int = {
    val result = decorated.read()
    if (result >= 0 && captured.size() < maxSize)
      captured.write(result)
    result
  }

}
