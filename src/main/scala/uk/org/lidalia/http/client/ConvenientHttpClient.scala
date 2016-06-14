package uk.org.lidalia.http.client

import uk.org.lidalia.http
import http.core.Method.{DELETE, GET, HEAD, TRACE}
import http.core.headerfields.Host
import uk.org.lidalia.http.core.{Accept, HeaderField, Method, Request, RequestUri}
import uk.org.lidalia.scalalang.ByteSeq
import uk.org.lidalia.net.{PathAndQuery, Url}

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

class ConvenientHttpClient[+Result[_]](decorated: HttpClient[Result]) extends HttpClient[Result] {

  def get[T](
    pathAndQuery: PathAndQuery,
    accept: Accept[T],
    headerFields: HeaderField*
  ) = execute(GET, pathAndQuery, accept, headerFields:_*)

  def get(
    pathAndQuery: PathAndQuery,
    headerFields: HeaderField*
  ) = execute(GET, pathAndQuery, headerFields:_*)

  def head(
    pathAndQuery: PathAndQuery,
    headerFields: HeaderField*) = {
    decorated.execute(
      Request(
        HEAD,
        RequestUri(pathAndQuery),
        headerFields.toList
      )
    )
  }

  def delete[T](
    pathAndQuery: PathAndQuery,
    accept: Accept[T],
    headerFields: HeaderField*) = execute(DELETE, pathAndQuery, accept, headerFields:_*)

  def delete(
     pathAndQuery: PathAndQuery,
     headerFields: HeaderField*) = execute(DELETE, pathAndQuery, headerFields:_*)

  def options[T](
    pathAndQuery: PathAndQuery,
    accept: Accept[T],
    headerFields: HeaderField*) = execute(TRACE, pathAndQuery, accept, headerFields:_*)

  def options(
    pathAndQuery: PathAndQuery,
    headerFields: HeaderField*) = execute(TRACE, pathAndQuery, headerFields:_*)

  def execute[T](
    method: Method,
    pathAndQuery: PathAndQuery,
    accept: Accept[T],
    headerFields: HeaderField*): Result[T] = {

    decorated.execute(
      requestFor(method, pathAndQuery, headerFields.toList, accept, List(
        accept
      ))
    )
  }

  def execute(
    method: Method,
    pathAndQuery: PathAndQuery,
    headerFields: HeaderField*
  ): Result[ByteSeq] = {
    decorated.execute(
      Request(
        method,
        RequestUri(pathAndQuery),
        headerFields.toList
      )
    )
  }

  private def requestFor[T](
    method: Method,
    pathAndQuery: PathAndQuery,
    headerFields: immutable.Seq[HeaderField],
    accept: Accept[T],
    baseFields: List[HeaderField]) =
  {
    Request(
      method,
      RequestUri(pathAndQuery),
      accept,
      baseFields ++ headerFields.toSeq
    )
  }

  override def executeClient[T](request: Request[T, _]) = decorated.execute(request)
}
