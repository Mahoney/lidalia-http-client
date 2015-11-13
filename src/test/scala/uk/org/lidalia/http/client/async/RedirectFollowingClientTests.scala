package uk.org.lidalia.http.client

import java.util.concurrent.TimeUnit.SECONDS

import org.{mockito, scalatest}
import mockito.BDDMockito.given
import mockito.Mockito.mock
import org.scalatest.{FunSuite, OneInstancePerTest}
import scalatest.prop.TableDrivenPropertyChecks
import uk.org.lidalia
import lidalia.http
import http.core.Code.Found
import http.core.RequestBuilder.get
import http.core.Response
import http.core.ResponseBuilder.response
import http.core.headerfields.Location
import lidalia.net.Url

import scala.concurrent.Future.successful
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RedirectFollowingClientTests extends FunSuite with TableDrivenPropertyChecks with OneInstancePerTest {

  val decorated = mock(classOf[RawHttpClient])
  val redirectFollowingHttpClient = new RedirectFollowingClient(decorated)

  def valueOf[T](future: Future[Response[T]]) = Await.result(future, Duration(10, SECONDS))

  test("Returns response from wrapped client when not a redirect") {
    val request = get()
    val expectedResponse = successful(response())
    given(decorated.execute(request)).willReturn(expectedResponse)

    val actualResponse = redirectFollowingHttpClient.execute(request)

    assert(valueOf(actualResponse) === valueOf(expectedResponse))
  }

  ignore("Follows 302 redirect") {
    val request1 = get()
    val redirectLocation = Url("http://www.example.com/redirectlocation")
    val redirectResponse = successful(response(Found, List(Location(redirectLocation))))

    val request2 = get(redirectLocation)
    val expectedResponse = successful(response())

    given(decorated.execute(request1)).willReturn(redirectResponse)
    given(decorated.execute(request2)).willReturn(expectedResponse)

    val actualResponse = redirectFollowingHttpClient.execute(request1)

    assert(valueOf(actualResponse) === valueOf(expectedResponse))
  }

  ignore("Follows two 302 redirects") {
    val request1 = get()
    val redirectLocation1 = Url("http://www.example.com/redirectlocation")
    val redirectResponse1 = successful(response(Found, List(Location(redirectLocation1))))

    val request2 = get(redirectLocation1)
    val redirectLocation2 = Url("http://www.example.com/redirectlocation2")
    val redirectResponse2 = successful(response(Found, List(Location(redirectLocation2))))

    val request3 = get(redirectLocation2)
    val expectedResponse = successful(response())

    given(decorated.execute(request1)).willReturn(redirectResponse1)
    given(decorated.execute(request2)).willReturn(redirectResponse2)
    given(decorated.execute(request3)).willReturn(expectedResponse)

    val actualResponse = redirectFollowingHttpClient.execute(request1)

    assert(valueOf(actualResponse) === valueOf(expectedResponse))
  }

  test("Copes with single redirect loop") {
    val redirectLocation1 = Url("http://www.example.com/redirectlocation")
    val redirectLocation2 = Url("http://www.example.com/redirectlocation2")
    val redirectLocation3 = Url("http://www.example.com/redirectlocation3")

    val request1 = get(redirectLocation3)
    val redirectResponse1 = successful(response(Found, List(Location(redirectLocation1))))

    val request2 = get(redirectLocation1)
    val redirectResponse2 = successful(response(Found, List(Location(redirectLocation2))))

    val request3 = get(redirectLocation2)
    val redirectResponse3 = successful(response(Found, List(Location(redirectLocation3))))

    val expectedResponse = successful(response())

    given(decorated.execute(request1)).willReturn(redirectResponse1, expectedResponse)
    given(decorated.execute(request2)).willReturn(redirectResponse2)
    given(decorated.execute(request3)).willReturn(redirectResponse3)

    val actualResponse = redirectFollowingHttpClient.execute(request1)

    assert(valueOf(actualResponse) === valueOf(expectedResponse))

  }

  test("Exits on endless redirect") {
    val redirectLocation1 = Url("http://www.example.com/redirectlocation")
    val redirectLocation2 = Url("http://www.example.com/redirectlocation2")
    val redirectLocation3 = Url("http://www.example.com/redirectlocation3")

    val request1 = get(redirectLocation3)
    val redirectResponse1 = successful(response(Found, List(Location(redirectLocation1))))

    val request2 = get(redirectLocation1)
    val redirectResponse2 = successful(response(Found, List(Location(redirectLocation2))))

    val request3 = get(redirectLocation2)
    val redirectResponse3 = successful(response(Found, List(Location(redirectLocation3))))

    given(decorated.execute(request1)).willReturn(redirectResponse1)
    given(decorated.execute(request2)).willReturn(redirectResponse2)
    given(decorated.execute(request3)).willReturn(redirectResponse3)

    val thrown = intercept[InfiniteRedirectException] {
      valueOf(redirectFollowingHttpClient.execute(request1))
    }

    assert(thrown.request === request1)
    assert(thrown.response === valueOf(redirectResponse1))

  }
}
