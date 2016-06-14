package uk.org.lidalia.http.client

import org.mockito.Mockito.mock
import org.scalatest.FunSuite
import uk.org.lidalia.http.client.ExpectedEntityHttpClient.FutureResponse
import uk.org.lidalia.http.core.{Request, Response}

import scala.concurrent.Future

class FutureHttpClientTests extends FunSuite {

  test("types are equivalent") {

    val client1: HttpClient[FutureResponse] = mock(classOf[HttpClient[FutureResponse]])
    val client2: FutureHttpClient[Response] = mock(classOf[FutureHttpClient[Response]])

    val request: Request[String, _] = mock(classOf[Request[String, _]])

    val result1: FutureResponse[String] = client1.execute(request)
    val result2: Future[Response[String]] = client2.execute(request)

    implicitly[FutureResponse[String] =:= Future[Response[String]]]
    implicitly[Future[Response[String]] =:= FutureResponse[String]]
    implicitly[result1.type <:< FutureResponse[String]]
    implicitly[result2.type <:< FutureResponse[String]]
  }

  test("higher kinded type algebra") {

    trait Future[+T1]
    trait Response[+T2]
    type FutureResponse[+T3] = Future[Response[T3]]

    trait HttpClient[+HK1[_]]

    trait FutureHttpClient[+HK3[_]] extends HttpClient[({type FutureHK[T]=Future[HK3[T]]})#FutureHK]

    implicitly[FutureHttpClient[Response] <:< HttpClient[FutureResponse]]

  }

}
