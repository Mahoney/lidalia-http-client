package uk.org.lidalia.http.client

import uk.org.lidalia.http.core.Request

import scala.concurrent.Future
import scala.language.higherKinds

trait FutureHttpClient[+Result[_]] extends HttpClient[({type FutureResult[T]=Future[Result[T]]})#FutureResult] {

  def executeClient[T](request: Request[T, _]): Future[Result[T]]

}
