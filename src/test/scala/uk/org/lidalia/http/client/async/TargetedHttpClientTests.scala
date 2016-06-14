package uk.org.lidalia.http.client

import java.io.{InputStream, InputStreamReader}
import java.util.concurrent.TimeUnit
import javax.json.{Json, JsonObject}

import org.apache.commons.io.IOUtils
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{Outcome, fixture}
import uk.org.lidalia.http.core.Method.GET
import uk.org.lidalia.http.core.headerfields.{ContentType, Date}
import uk.org.lidalia.http.core.{Accept, AnyEntity, Code, HeaderField, MediaRange, MediaRangePref, Request, RequestUri, Response, ResponseHeader, StringEntity}
import uk.org.lidalia.stubhttp.{DSL, StubHttpServer, StubHttpServerFactory}

import scala.concurrent.{Await, TimeoutException}
import scala.concurrent.duration.Duration

class TargetedHttpClientTests extends fixture.FunSuite {

  val unmarshaller = new Accept[String](List(new MediaRangePref(new MediaRange("text/plain")))) {
    override def unmarshal(request: Request[_, _], response: ResponseHeader, entityBytes: InputStream) = new StringEntity(IOUtils.toString(entityBytes))
  }

  test("Returns response from server") { case (server, client) =>
    server.stub(
      DSL.get("/foo").returns(
        Response(
          200,
          Date:= "Sun, 06 Nov 1994 08:49:37 GMT",
          ContentType:= "text/plain"
        )(
          "Some text"
        )
      )
    )

    val request = Request(GET, RequestUri("/foo"), unmarshaller, List())
    val response = Await.result(
      client.execute(request),
      Duration(1, TimeUnit.SECONDS)
    )


    assert(
      response.code === Code(200) &&
      response.headerField("Content-Type") === Some(HeaderField("Content-Type", "text/plain")) &&
//      response.date === Some(new DateTime("1994-11-06T08:49:37").withZone(DateTimeZone.forID("GMT"))) &&
      response.entity === Right("Some text")
    )
  }

  ignore("Cancelling future disconnects") { case (server, client) =>

    server.stub(
      DSL.get("/foo").returns(
        Response(
          200,
          Date:= "Sun, 06 Nov 1994 08:49:37 GMT",
          ContentType:= "text/plain"
        )(
          "Some text"
        )
      )
    )

    val request = Request(GET, RequestUri("/foo"), unmarshaller, List())

    try {
      val response = Await.result(
        client.execute(request),
        Duration(10, TimeUnit.MILLISECONDS)
      )
      fail("Should have timed out!")
    } catch {
      case e: TimeoutException => fail("prove here that the connection has gone...")
    }
  }

  test("Failure to unmarshal returns body as Left") { case (server, client) =>

    server.stub(
      DSL.get("/foo").returns(
        Response(
          200,
          Date:= "Sun, 06 Nov 1994 08:49:37 GMT",
          ContentType:= "application/json"
        )(
          "Not json!"
        )
      )
    )

    val request = Request(
      GET,
      RequestUri("/foo"),
      new Accept[JsonObject](List(new MediaRangePref(new MediaRange("application/json")))) {
        def unmarshal(request: Request[_, _], response: ResponseHeader, entityBytes: InputStream) = new AnyEntity(Json.createReader(new InputStreamReader(entityBytes, "UTF-8")).readObject())
      },
      List()
    )

    val response = Await.result(
      client.execute(request),
      Duration(1, TimeUnit.SECONDS)
    )
    assert(
      response.code === Code(200) &&
      response.headerField("Content-Type") === Some(HeaderField("Content-Type", "application/json")) &&
//      response.date === Some(new DateTime("1994-11-06T08:49:37").withZone(DateTimeZone.forID("GMT"))) &&
      response.entity === Left("Not json!")
    )
  }

  override type FixtureParam = (StubHttpServer, Apache4Client)

  override protected def withFixture(test: OneArgTest): Outcome = {
    StubHttpServerFactory().using { server =>
      val client = Apache4Client(server.localAddress)
      test((server, client))
    }
  }
}


