package uk.org.lidalia.http.client

import uk.org.lidalia.http.core.{Request, Response}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RedirectFollowingClient(decorated: RawHttpClient) extends RawHttpClient {

  override def executeClient[T](request: Request[T, _]): Future[Response[Either[String, T]]] = execute(request, List())

  private def execute[T](request: Request[T, _], history: List[Request[T, _]]): Future[Response[Either[String, T]]] = {

    val initialResponse = decorated.execute(request)

    def followRedirect(response: Response[Either[String, T]]) =
      response.location.map(location => {
        if (history.contains(request)) {
          throw InfiniteRedirectException(response, request)
        } else {
          execute(request, request :: history)
        }
      }).getOrElse(initialResponse)

    initialResponse.flatMap { response =>
      if (!response.requiresRedirect) {
        initialResponse
      } else {
        followRedirect(response)
      }
    }
  }
}
