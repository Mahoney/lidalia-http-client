package uk.org.lidalia.http.client

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.Outcome
import uk.org.lidalia.http.client.DefaultSyncHttpClient.get
import uk.org.lidalia.http.core.MediaType.`text/plain`
import uk.org.lidalia.http.core.headerfields.{ContentType, Date}
import uk.org.lidalia.http.core.{Code, Response}
import uk.org.lidalia.net.Url
import uk.org.lidalia.stubhttp.{DSL, StubHttpServer, StubHttpServerFactory}

class DefaultSyncHttpClientTests extends org.scalatest.fixture.FunSuite {

  test("can get bytes") { server =>
    server.stub(
      DSL.get("/foo")
      .returns(
        Response(
          200,
          Date:= "Sun, 06 Nov 1994 08:49:37 GMT",
          ContentType:= "text/plain"
        )(
          "Some text"
        )
      )
    )

    val response = get(Url(server.localAddress.toString ++ "/foo"))

    assert(
      response.code == Code(200) &&
      response.contentType.contains(`text/plain`) &&
//      response.date.contains(new DateTime("1994-11-06T08:49:37.000Z").withZone(DateTimeZone.forID("GMT"))) &&
      response.entityString == "Some text"
    )
  }

  override type FixtureParam = StubHttpServer

  override protected def withFixture(test: OneArgTest): Outcome = {
    StubHttpServerFactory().using { server =>
      test(server)
    }
  }
}
