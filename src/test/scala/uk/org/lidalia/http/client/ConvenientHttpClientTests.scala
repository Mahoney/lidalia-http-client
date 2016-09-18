package uk.org.lidalia.http.client

import java.io.InputStream

import org.apache.commons.io.IOUtils
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.scalatest.FunSuite
import uk.org.lidalia.http.client.EntityOnlyHttpClient.Is
import uk.org.lidalia.http.core.Code.OK
import uk.org.lidalia.http.core.Method.{GET, HEAD}
import uk.org.lidalia.http.core.{Accept, ByteEntity, Request, RequestUri, Response, ResponseHeader, StringEntity}
import uk.org.lidalia.http.core.headerfields.{Etag, Host}
import uk.org.lidalia.net.{HostAndPort, PathAndQuery}
import uk.org.lidalia.scalalang.ByteSeq

class ConvenientHttpClientTests extends FunSuite {

  val decoratedClient = mock(classOf[HttpClient[Is]])
  val client = new ConvenientHttpClient(decoratedClient)

  val accept: Accept[String] = new Accept[String](List()) {
    override def unmarshal(request: Request[_, _], response: ResponseHeader, entityBytes: InputStream) = new StringEntity(IOUtils.toString(entityBytes))
  }

  test("Makes get request") {

    given(decoratedClient.execute(
      Request(
        GET,
        RequestUri("/blah"),
        accept,
        List(
          Host := HostAndPort("localhost"),
          Etag := "my-custom-etag"
        )
      )
    )).willReturn("Result")

    val result: String = client.get(
      PathAndQuery("/blah"),
      accept,
      Host := HostAndPort("localhost"),
      Etag := "my-custom-etag"
    )

    assert(result === "Result")
  }

  test("Makes head request") {

    val decoratedClient = mock(classOf[HttpClient[Response]])
    val client = new ConvenientHttpClient(decoratedClient)

    given(decoratedClient.execute(
      Request(
        HEAD,
        RequestUri("/blah"),
        List(
          accept,
          Host := HostAndPort("localhost"),
          Etag := "my-custom-etag"
        )
      )
    )).willReturn(
      Response(
        OK,
        List(),
        new ByteEntity(ByteSeq())
      )
    )

    val result = client.head(
      PathAndQuery("/blah"),
      accept,
      Host := HostAndPort("localhost"),
      Etag := "my-custom-etag"
    )

    assert(result === Response(OK, List(), new ByteEntity(ByteSeq())))
  }
}
