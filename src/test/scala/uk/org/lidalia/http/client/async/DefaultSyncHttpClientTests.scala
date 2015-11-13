package uk.org.lidalia.http.client

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.FunSuite
import uk.org.lidalia.http.client.DefaultSyncHttpClient.get
import uk.org.lidalia.http.core.MediaType.`text/plain`
import uk.org.lidalia.http.core.headerfields.{ContentType, Date}
import uk.org.lidalia.http.core.{Code, Response}
import uk.org.lidalia.net.Url
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.TestLoggerFactory
import uk.org.lidalia.stubhttp.{DSL, StubHttpServerFactory}
import uk.org.lidalia.scalalang.WithResourceTests

class DefaultSyncHttpClientTests extends FunSuite with WithResourceTests {

  TestLoggerFactory.getInstance().setPrintLevel(Level.INFO)

  test("can get bytes", StubHttpServerFactory()) { server =>
    server.stub(
      DSL.get("/foo")
      .returns(Response(
        200,
        Date:= "Sun, 06 Nov 1994 08:49:37 GMT",
        ContentType:= "text/plain"
      )(
        "Some text"
      ))
    )

    def response = get(Url(server.localAddress.toString ++ "/foo"))

    assert(response.code == Code(200))
    assert(response.contentType.contains(`text/plain`))
    assert(response.date.contains(new DateTime("1994-11-06T08:49:37.000Z").withZone(DateTimeZone.forID("GMT"))))
    assert(response.entityString == "Some text")
  }
}
