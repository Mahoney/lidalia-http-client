package uk.org.lidalia.http.client

import java.io.{ByteArrayOutputStream, InputStream}
import java.net.InetAddress

import com.google.common.base.Charsets
import org.apache.commons.io.IOUtils
import uk.org.lidalia
import lidalia.http
import lidalia.http.core.{EntityUnmarshaller, AnyEntity, Code, EitherEntity, Entity, HeaderField, Reason, Request, Response, ResponseHeader}
import lidalia.net.{IpAddress, Url}
import org.apache
import apache.http.client.{ResponseHandler => ApacheResponseHandler}
import apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import apache.http.message.BasicHttpRequest
import apache.http.{HttpHost, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Apache4Client {

  def apply(
    target: Url,
    apacheClient: CloseableHttpClient = HttpClientBuilder.create()
      .setMaxConnPerRoute(Integer.MAX_VALUE)
      .setMaxConnTotal(Integer.MAX_VALUE)
      .disableRedirectHandling()
      .build()
  ) = {
    new Apache4Client(
      target,
      apacheClient
    )
  }
}

class Apache4Client private (
   target: Url,
   apacheClient: CloseableHttpClient
) extends RawHttpClient {

  override def execute[T](request: Request[T, _]): Future[Response[Either[String, T]]] = {
    Future {

      val apacheHost = new HttpHost(
        IpAddress(InetAddress.getByName(target.host.toString)).toString,
        target.resolvedPort.portNumber,
        target.scheme.toString
      )

      val apacheRequest = new BasicHttpRequest(
        request.method.toString,
        request.requestUri.toString
      )
      
      val apacheResponseHandler = new ApacheResponseHandler[Response[Either[String, T]]] {



        def handleResponse(response: HttpResponse): Response[Either[String, T]] = {
          val headerFields = response.getAllHeaders.map{
            headerField => HeaderField(headerField.getName, headerField.getValue)
          }.toList

          val responseHeader = ResponseHeader(
            Code(response.getStatusLine.getStatusCode),
            Reason(response.getStatusLine.getReasonPhrase),
            headerFields
          )
          val entity: Entity[Either[String, T]] = unmarshal(request, response, responseHeader, request.unmarshaller)
          Response(responseHeader, entity)
        }
      }

      apacheClient.execute(
        apacheHost,
        apacheRequest,
        apacheResponseHandler
      )
    }
  }

  def unmarshal[T](request: Request[T, _], response: HttpResponse, responseHeader: ResponseHeader, unmarshaller: EntityUnmarshaller[T]): Entity[Either[String, T]] = {

    val content = new CapturingInputStream(response.getEntity.getContent)
    try {
      new EitherEntity(Right(unmarshaller.unmarshal(request, responseHeader, content)))
    } catch {
      case e: Exception =>
        val array: Array[Byte] = content.captured.toByteArray
        val charset = responseHeader.contentType.flatMap(_.charset).getOrElse(Charsets.UTF_8)
        new EitherEntity(Left(new AnyEntity(IOUtils.toString(array, charset.name()))))
    }
  }
}

class CapturingInputStream(decorated: InputStream, maxSize: Int = 1024 * 512) extends InputStream {

  var firstByte: ?[Int] = None
  var firstByteRead = false

  val captured = new ByteArrayOutputStream(maxSize)

  override def read(): Int = {
    if (firstByteRead) {
      doRead()
    } else {
      firstByteRead = true
      getFirstByte
    }
  }

  def doRead(): Int = {
    val result = decorated.read()
    if (result >= 0 && captured.size() < maxSize)
      captured.write(result)
    result
  }

  def isEmpty = getFirstByte == -1

  private def getFirstByte = {
    if (firstByte.isEmpty) {
      firstByte = Some(doRead())
    }
    firstByte.get
  }
}
