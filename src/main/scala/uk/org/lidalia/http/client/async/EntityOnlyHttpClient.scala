package uk.org.lidalia.http.client

import uk.org.lidalia.http.client.ExpectedEntityHttpClient.FutureResponse
import uk.org.lidalia.http.core.Request
import uk.org.lidalia.net.Url

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EntityOnlyHttpClient {
  type Is[T] = T

  def apply(baseUrl: Url): Unit = {
    apply(ExpectedEntityHttpClient(baseUrl))
  }

  def apply(decorated: HttpClient[FutureResponse]) = {
    new EntityOnlyHttpClient(decorated)
  }
}

class EntityOnlyHttpClient private (
  decorated: HttpClient[FutureResponse]
) extends HttpClient[Future] {

  override def executeClient[T](request: Request[T, _]): Future[T] = {
    decorated.execute(request).map(_.entity)
  }
}
