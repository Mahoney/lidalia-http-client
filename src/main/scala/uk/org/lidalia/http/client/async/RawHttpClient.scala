package uk.org.lidalia.http.client

import uk.org.lidalia.http.client.RawHttpClient.{FutureResponseStringOr, ResponseStringOr}
import uk.org.lidalia.http.core.{Request, Response}
import uk.org.lidalia.net.Url

import scala.concurrent.Future

object RawHttpClient {
  type StringOr[T] = Either[String, T]
  type ResponseStringOr[T] = Response[StringOr[T]]
  type FutureResponseStringOr[T] = Future[ResponseStringOr[T]]

  def apply(baseUrl: Url) = Apache4Client(baseUrl)

}

trait RawHttpClient extends HttpClient[FutureResponseStringOr] with FutureHttpClient[ResponseStringOr] {
  def execute[T](request: Request[T, _]): Future[Response[Either[String, T]]]
}
