package dinis.anatoliy.paidyrestaurant

import cats.effect.Sync
import fs2.Stream

// Stream Utils helper functions taken from http4s examles github repository
trait StreamUtils[F[_]] {
  def evalF[A](thunk: => A)(implicit F: Sync[F]): Stream[F, A] = Stream.eval(F.delay(thunk))
  def putStrLn(value: String)(implicit F: Sync[F]): Stream[F, Unit] = evalF(println(value))
  def putStr(value: String)(implicit F: Sync[F]): Stream[F, Unit] = evalF(print(value))
  def env(name: String)(implicit F: Sync[F]): Stream[F, Option[String]] = evalF(sys.env.get(name))
  def error(msg: String)(implicit F: Sync[F]): Stream[F, String] =
    Stream.raiseError(new Exception(msg)).covary[F]
}

object StreamUtils {
  implicit def syncInstance[F[_]: Sync]: StreamUtils[F] = new StreamUtils[F] {}
}
