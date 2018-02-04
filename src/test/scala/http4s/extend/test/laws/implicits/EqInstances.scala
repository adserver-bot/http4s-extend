package http4s.extend.test.laws.implicits

import cats.Eq
import cats.effect.IO
import cats.instances.string._
import cats.syntax.either._
import http4s.extend.ErrorAdapt
import http4s.extend.syntax.eq._
import http4s.extend.test.Fixtures

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

trait EqInstances extends Fixtures {

  implicit val throwableEq: Eq[Throwable] =
    Eq.by[Throwable, String](_.getMessage)

  implicit val testErrorEq: Eq[TestError] =
    Eq.by[TestError, String](_.error)

  implicit def futureEqual[A: Eq](implicit ec: ExecutionContext): Eq[Future[A]] = {

    def futureEither(fa: Future[A]): Future[Either[Throwable, A]] =
      ErrorAdapt[Future].attemptMapLeft(fa)(identity[Throwable])

    new Eq[Future[A]] {
      def eqv(x: Future[A], y: Future[A]): Boolean =
        Await.result(
          futureEither(x) zip futureEither(y) map { case (tx, ty) => tx === ty },
          1.second
        )
    }
  }

  implicit def ioEqual[A: Eq]: Eq[IO[A]] =
    new Eq[IO[A]] {
      def eqv(x: IO[A], y: IO[A]): Boolean = {

        val xv = Either.catchNonFatal(x.attempt.unsafeRunSync())
        val yv = Either.catchNonFatal(y.attempt.unsafeRunSync())

        (xv, yv) match {
          case (Right(vl) , Right(vr))  => vl === vr
          case (Left(el)  , Left(er))   => el === er
          case _                        => false
        }
      }
    }
}