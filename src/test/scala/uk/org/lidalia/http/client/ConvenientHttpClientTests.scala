package uk.org.lidalia.http.client

import java.io.InputStream

import org.apache.commons.io.IOUtils
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.scalatest.PropSpec
import uk.org.lidalia.http.client.EntityOnlyHttpClient.Is
import uk.org.lidalia.http.core.Code.OK
import uk.org.lidalia.http.core.Method.{GET, HEAD}
import uk.org.lidalia.http.core.{Accept, ByteEntity, EmptyEntity, Request, RequestUri, Response, ResponseHeader, StringEntity}
import uk.org.lidalia.http.core.headerfields.{Etag, Host}
import uk.org.lidalia.net.Scheme.http
import uk.org.lidalia.net.{HostAndPort, PathAndQuery, Url}
import uk.org.lidalia.scalalang.ByteSeq

class ConvenientHttpClientTests extends PropSpec {

  val decoratedClient = mock(classOf[HttpClient[Is]])
  val client = new ConvenientHttpClient(decoratedClient)

  property("Default has expected type") {
    val client = ConvenientHttpClient(Url("http://localhost"))
  }

  property("Makes get request") {
    val accept: Accept[String] = new Accept[String](List()) {
      override def unmarshal(request: Request[_, _], response: ResponseHeader, entityBytes: InputStream) = new StringEntity(IOUtils.toString(entityBytes))
    }
    val request = Request(
      GET,
      RequestUri("/blah"),
      accept,
      List(
        Host := HostAndPort("localhost"),
        Etag := "my-custom-etag"
      )
    )

    given(decoratedClient.execute(
      request
    )).willReturn("Result")

    val result: String = client.get(
      PathAndQuery("/blah"),
      accept,
      Host := HostAndPort("localhost"),
      Etag := "my-custom-etag"
    )

    assert(result === "Result")
  }

  property("Makes head request") {
    val decoratedClient = mock(classOf[HttpClient[Response]])
    val client = new ConvenientHttpClient(decoratedClient)
    val accept: Accept[String] = new Accept[String](List()) {
      override def unmarshal(request: Request[_, _], response: ResponseHeader, entityBytes: InputStream) = new StringEntity(IOUtils.toString(entityBytes))
    }

    val request = Request(
      HEAD,
      RequestUri("/blah"),
      List(
        accept,
        Host := HostAndPort("localhost"),
        Etag := "my-custom-etag"
      )
    )
    val expectedResponse = Response(OK, List(), new ByteEntity(ByteSeq()))
    given(decoratedClient.execute(
      request
    )).willReturn(expectedResponse)

    val result = client.head(
      PathAndQuery("/blah"),
      accept,
      Host := HostAndPort("localhost"),
      Etag := "my-custom-etag"
    )

    assert(result === expectedResponse)
  }
}
