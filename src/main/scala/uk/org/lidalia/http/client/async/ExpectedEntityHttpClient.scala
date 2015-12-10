package uk.org.lidalia.http.client

import uk.org.lidalia.http.client.ExpectedEntityHttpClient.FutureResponse
import uk.org.lidalia.http.core.{EitherEntity, Request, Response}
import uk.org.lidalia.net.Url

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ExpectedEntityHttpClient {

  type FutureResponse[T] = Future[Response[T]]

  def apply(
    baseUrl: Url
  ): ExpectedEntityHttpClient = {
    ExpectedEntityHttpClient(
      ThrowClientErrorHttpClient(
        ThrowServerErrorHttpClient(
            Apache4Client(baseUrl)
        )
      )
    )
  }

  def apply(
    decorated: RawHttpClient
  ) = {
    new ExpectedEntityHttpClient(decorated)
  }
}

class ExpectedEntityHttpClient private (
  decorated: RawHttpClient
) extends HttpClient[FutureResponse] with FutureHttpClient[Response] {

  def executeClient[A](request: Request[A, _]): Future[Response[A]] = {
    val futureResponse = decorated.execute(request)
    futureResponse.map(response => {
      val eitherEntity = response.marshallableEntity.asInstanceOf[EitherEntity[String, A]]
      eitherEntity.eitherEntity match {
        case Left(error) => throw new Exception(Response(response.header, error).toString)
        case Right(success) => Response(response.header, success)
      }
    })
  }
}
