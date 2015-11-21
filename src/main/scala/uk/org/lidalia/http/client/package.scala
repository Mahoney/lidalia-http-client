package uk.org.lidalia.http

import scala.annotation.meta.field
import scala.language.implicitConversions

package object client {

  type ?[T] = Option[T]
  type Identity = uk.org.lidalia.lang.Identity @field

  implicit def instanceToSome[T](instance: T) = Some(instance)
  implicit def someToInstance[T](some: Some[T]) = some.get
  implicit def typeToLeft[A,B](instance: A): Either[A,B] = Left(instance)
  implicit def typeToRight[A,B](instance: B): Either[A,B] = Right(instance)

  class ToRichOption[+A](val option: Option[A]) {
    def or[B >: A](default: => B): B = option.getOrElse(default)
    def ?[B](f: A => ?[B]): ?[B] = option.flatMap(f)
  }
  implicit def toRichOption[A](option: Option[A]) = new ToRichOption(option)

  lazy val DefaultSyncHttpClient = ConvenientMultiTargetHttpClient(
    MultiTargetHttpClient((url) => SyncHttpClient(url))
  )

  lazy val DefaultHttpClient = ConvenientMultiTargetHttpClient()
}
