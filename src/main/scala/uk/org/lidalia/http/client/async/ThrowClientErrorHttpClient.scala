package uk.org.lidalia.http.client

import uk.org.lidalia.http.core.{Request, Response}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ThrowClientErrorHttpClient {

  def apply(decorated: RawHttpClient) = new ThrowClientErrorHttpClient(decorated)

}

class ThrowClientErrorHttpClient private (
  decorated: RawHttpClient
) extends RawHttpClient {

   def executeClient[A](request: Request[A, _]): Future[Response[Either[String, A]]] = {
     val futureResponse = decorated.execute(request)
     futureResponse.map(response => {
       if (response.isClientError) throw ClientError(response, request)
       else response
     })
   }
 }
