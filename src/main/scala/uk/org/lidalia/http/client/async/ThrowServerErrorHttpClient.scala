package uk.org.lidalia.http.client

import uk.org.lidalia.http.client.RawHttpClient.FutureResponseStringOr
import uk.org.lidalia.http.core.{Request, Response}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ThrowServerErrorHttpClient {

  def apply(decorated: HttpClient[FutureResponseStringOr]) = new ThrowServerErrorHttpClient(decorated)

}

class ThrowServerErrorHttpClient private (
  decorated: HttpClient[FutureResponseStringOr]
) extends RawHttpClient {

  def executeClient[A](request: Request[A, _]): Future[Response[Either[String, A]]] = {
    val futureResponse = decorated.execute(request)
    futureResponse.map(response => {
      if (response.isServerError) throw ServerError(response, request)
      else response
    })
  }
}
