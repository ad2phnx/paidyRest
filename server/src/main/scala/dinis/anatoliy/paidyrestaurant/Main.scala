package dinis.anatoliy.paidyrestaurant

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.Json
import org.http4s.HttpRoutes
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.blaze._

// Main server class
// Sets up order repository, routes for orders
// and starts Blaze server on localhost:8080
object Main extends IOApp {

  private val orderRepo: OrderRepo = new OrderRepo.OrderImpl

  val httpRoutes = Router[IO](
    "/" -> Routes.routes(orderRepo)
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {

    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
