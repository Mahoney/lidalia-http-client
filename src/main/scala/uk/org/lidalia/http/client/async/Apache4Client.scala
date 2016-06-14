package uk.org.lidalia
package http.client

import java.net.InetAddress

import com.google.common.base.Charsets
import org.apache.commons.io.IOUtils
import uk.org.lidalia
import lidalia.http
import http.core.{AnyEntity, Code, EitherEntity, Entity, EntityUnmarshaller, HeaderField, Reason, Request, Response, ResponseHeader}
import lidalia.net.{IpAddress, Url}
import org.apache
import apache.http.client.{ResponseHandler => ApacheResponseHandler}
import apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import apache.http.message.BasicHttpRequest
import apache.http.{HttpHost, HttpResponse}
import http.client.async.CapturingInputStream
import uk.org.lidalia.scalalang.CloseableResourceFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Apache4Client {

  def apply(
    target: Url,
    apacheClient: HttpClientBuilder = HttpClientBuilder.create()
      .setMaxConnPerRoute(Integer.MAX_VALUE)
      .setMaxConnTotal(Integer.MAX_VALUE)
      .disableRedirectHandling()
  ) = {
    new Apache4Client(
      target,
      new CloseableResourceFactory(() => apacheClient.build())
    )
  }
}

class Apache4Client private (
   target: Url,
   apacheClientFactory: CloseableResourceFactory[CloseableHttpClient]
) extends RawHttpClient {

  override def executeClient[T](request: Request[T, _]): Future[Response[Either[String, T]]] = {
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
      request.headerFields.foreach { headerField =>
        apacheRequest.addHeader(headerField.name, headerField.valueString)
      }

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

      apacheClientFactory.using { apacheClient =>
        apacheClient.execute(
          apacheHost,
          apacheRequest,
          apacheResponseHandler
        )
      }
    }
  }

  def unmarshal[T](request: Request[T, _], response: HttpResponse, responseHeader: ResponseHeader, unmarshaller: EntityUnmarshaller[T]): Entity[Either[String, T]] = {

    new CloseableResourceFactory(response.getEntity.getContent).using { inputStream =>
      val content = new CapturingInputStream(inputStream)
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
}
