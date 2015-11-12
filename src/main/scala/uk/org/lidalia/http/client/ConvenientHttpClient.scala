package uk.org.lidalia.http.client

import uk.org.lidalia.http
import http.core.Method.{DELETE, GET, HEAD, TRACE}
import http.core.headerfields.Host
import uk.org.lidalia.http.core.{Accept, HeaderField, Method, Request, RequestUri}
import uk.org.lidalia.scalalang.ByteSeq
import uk.org.lidalia.net2.Url

import scala.collection.immutable
import scala.language.higherKinds

object ConvenientHttpClient {
  def apply[Result[_]](
    decorated: HttpClient[Result]
  ) = new ConvenientHttpClient(decorated)

  def apply(
    baseUrl: Url
  ) = {
    new ConvenientHttpClient(ExpectedEntityHttpClient(baseUrl))
  }
}

class ConvenientHttpClient[Result[_]](decorated: HttpClient[Result]) extends HttpClient[Result] {

  def get[T](
    url: Url,
    accept: Accept[T],
    headerFields: HeaderField*
  ) = execute(GET, url, accept, headerFields:_*)

  def get(
    url: Url,
    headerFields: HeaderField*
  ) = execute(GET, url, headerFields:_*)

  def head(
    url: Url,
    headerFields: HeaderField*) = {
    decorated.execute(
      Request(
        HEAD,
        RequestUri(url.pathAndQuery),
        List(Host := url.hostAndPort) ++ headerFields.toSeq
      )
    )
  }

  def delete[T](
    url: Url,
    accept: Accept[T],
    headerFields: HeaderField*) = execute(DELETE, url, accept, headerFields:_*)

  def delete(
     url: Url,
     headerFields: HeaderField*) = execute(DELETE, url, headerFields:_*)

  def options[T](
    url: Url,
    accept: Accept[T],
    headerFields: HeaderField*) = execute(TRACE, url, accept, headerFields:_*)

  def options(
    url: Url,
    headerFields: HeaderField*) = execute(TRACE, url, headerFields:_*)

  def execute[T](
    method: Method,
    url: Url,
    accept: Accept[T],
    headerFields: HeaderField*): Result[T] = {

    decorated.execute(
      requestFor(method, url, headerFields.toList, accept, List(
        Host := url.hostAndPort,
        accept
      ))
    )
  }

  def execute(
    method: Method,
    url: Url,
    headerFields: HeaderField*
  ): Result[ByteSeq] = {
    decorated.execute(
      Request(
        method,
        RequestUri(url.pathAndQuery),
        List(Host := url.hostAndPort) ++ headerFields.toSeq
      )
    )
  }

  private def requestFor[T](
    method: Method,
    url: Url,
    headerFields: immutable.Seq[HeaderField],
    accept: Accept[T],
    baseFields: List[HeaderField]) =
  {
    Request(
      method,
      RequestUri(url.pathAndQuery),
      accept,
      baseFields ++ headerFields.toSeq
    )
  }

  override def execute[T](request: Request[T, _]) = decorated.execute(request)
}
