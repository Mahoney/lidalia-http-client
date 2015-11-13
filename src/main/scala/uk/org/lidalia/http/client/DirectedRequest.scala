package uk.org.lidalia.http.client

import uk.org.lidalia.http.core.{EntityUnmarshaller, Method, Request, RequestUri}
import uk.org.lidalia.lang.RichObject
import uk.org.lidalia.net.{HostAndPort, Scheme, Url}

object DirectedRequest {

  def apply[T](
    scheme: Scheme,
    hostAndPort: HostAndPort,
    request: Request[T, _],
    unmarshaller: EntityUnmarshaller[T],
    parent: ?[DirectedRequest[T]] = None
  ) = {
    new DirectedRequest[T](
      scheme,
      hostAndPort,
      request,
      unmarshaller,
      parent
    )
  }
}

class DirectedRequest[T] private (
  @Identity val scheme: Scheme,
  @Identity val hostAndPort: HostAndPort,
  @Identity val request: Request[T, _],
  val unmarshaller: EntityUnmarshaller[T],
  val parent: ?[DirectedRequest[T]]
) extends RichObject {

  def redirected(location: Url, method: Method = request.method): DirectedRequest[T] =
    new DirectedRequest(
      location.scheme,
      location.hostAndPort,
      request.withUri(RequestUri(location.pathAndQuery)).withMethod(method),
      unmarshaller,
      Some(this)
    )
}
