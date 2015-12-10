package uk.org.lidalia.http.client

import uk.org.lidalia.http.core.{Http, Request}
import uk.org.lidalia.net.Url

import scala.language.higherKinds

object HttpClient {
  def apply(baseUrl: Url) = ExpectedEntityHttpClient(baseUrl)
}

trait HttpClient[+Result[_]] extends Http[Result] {

  def executeClient[T](request: Request[T, _]): Result[T]

  override def execute[A, C](request: Request[A, C]): Result[A] = executeClient(request)
}
