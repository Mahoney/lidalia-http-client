package uk.org.lidalia.http.client

import uk.org.lidalia.http.core.Method.GET
import uk.org.lidalia.http.core.{Method, Request, RequestUri}
import uk.org.lidalia.scalalang.ByteSeq
import uk.org.lidalia.net.Url

import scala.language.higherKinds

object ConvenientMultiTargetHttpClient {

  def apply[Result[_]](
    delegate: MultiTargetHttpClient[Result] = MultiTargetHttpClient()
  ) = {
    new ConvenientMultiTargetHttpClient(delegate)
  }
}

class ConvenientMultiTargetHttpClient[Result[_]] private (
  delegate: MultiTargetHttpClient[Result]
) {

  def get(
    url: Url
  ): Result[ByteSeq] = {
    execute(GET, url)
  }

  def execute(method: Method, url: Url): Result[ByteSeq] = {
    delegate.execute(url, Request(method, RequestUri(Right(url.pathAndQuery)), Nil))
  }
}
