package uk.org.lidalia.http.client

import uk.org.lidalia.http.core.{HeaderField, Request}

object HeaderSettingHttpClient {

  def apply[Result[_]](
    decorated: HttpClient[Result],
    headerField: HeaderField
  ) = {
    new HeaderSettingHttpClient(
      decorated, headerField
    )
  }
}

class HeaderSettingHttpClient[+Result[_]] private(
  decorated: HttpClient[Result],
  headerField: HeaderField
) extends HttpClient[Result] {

  override def executeClient[T](request: Request[T, _]): Result[T] = {

    val requestWithHeaderField = request.headerField(headerField.name).map { _ =>
      request
    }.getOrElse {
      request.withHeaderField(headerField)
    }

    decorated.executeClient(requestWithHeaderField)
  }

}
