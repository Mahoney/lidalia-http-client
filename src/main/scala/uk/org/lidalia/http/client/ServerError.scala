package uk.org.lidalia.http.client

import java.lang.System.lineSeparator

import uk.org.lidalia.http.core.{Request, Response}

import scala.language.higherKinds

object ServerError {
  def apply(response: Response[_], request: Request[_, _]) = new ServerError(response, request)
}

class ServerError private (response: Response[_], request: Request[_, _]) extends HttpResponseCodeError(response, request)

object ClientError {
  def apply(response: Response[_], request: Request[_, _]) = new ClientError(response, request)
}

class ClientError private (response: Response[_], request: Request[_, _]) extends HttpResponseCodeError(response, request)

class HttpResponseCodeError(response: Response[_], request: Request[_, _]) extends HttpException(response, request)

class HttpException(val response: Response[_], val request: Request[_, _]) extends Exception(response+lineSeparator()+"for"+lineSeparator()+request.toString)

object InfiniteRedirectException {
  def apply(response: Response[_], request: Request[_, _]) = new InfiniteRedirectException(response, request)
}

class InfiniteRedirectException private (response: Response[_], request: Request[_, _]) extends HttpException(response, request)
