package uk.org.lidalia.http.client

import uk.org.lidalia.http.core.Method._
import uk.org.lidalia.http.core.{RequestUri, Request, Method}
import uk.org.lidalia.lang.ByteSeq
import uk.org.lidalia.net2.Url

class ConvenientMultiTargetHttpClient[Result[_]](
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