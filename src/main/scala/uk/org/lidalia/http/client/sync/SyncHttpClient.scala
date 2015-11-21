package uk.org.lidalia.http.client

import java.util.concurrent.TimeUnit.MILLISECONDS

import org.joda.time.Duration
import uk.org.lidalia.http
import http.core.{Request, Response}
import uk.org.lidalia.net.Url

import scala.language.higherKinds
import scala.concurrent.Await
import scala.concurrent.{ duration => conc }

object SyncHttpClient {

  def apply(
    baseUrl: Url,
    timeout: Duration = Duration.standardSeconds(5)
  ): SyncHttpClient[Response] = {
    apply(
      ExpectedEntityHttpClient(baseUrl),
      timeout
    )
  }

  def apply[Result[_]](
    asyncHttpClient: FutureHttpClient[Result],
    timeout: Duration
  ): SyncHttpClient[Result] = {
    new SyncHttpClient(
      asyncHttpClient,
      timeout
    )
  }

  def apply[Result[_]](
    asyncHttpClient: FutureHttpClient[Result]
  ): SyncHttpClient[Result] = {
    apply(
      asyncHttpClient,
      Duration.standardSeconds(5)
    )
  }
}

class SyncHttpClient[+Result[_]] private (
  asyncHttpClient: FutureHttpClient[Result],
  timeout: Duration
) extends HttpClient[Result] {

  private val scalaTimeout = conc.Duration(timeout.getMillis, MILLISECONDS)

  def execute[T](
    request: Request[T, _]
  ): Result[T] = {

    val response = asyncHttpClient.execute(request)

    Await.result(response, scalaTimeout)
  }
}
