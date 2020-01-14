package dinis.anatoliy.paidyrestaurant

import cats.effect._
import cats.implicits._
import io.circe.Json
import io.circe.Encoder
import org.http4s.EntityEncoder
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.circe.CirceEntityEncoder
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.Uri.uri
import io.circe._
import scala.concurrent.ExecutionContext.Implicits.global

// Main client class, either takes in a string representing an array of json order objects
// like so: [{table: tableName, name: dishName}, ...] or creates one order of pizza on table 1
// if no arguments passed in.
object ClientPostExample extends IOApp with Http4sClientDsl[IO] {
  def run(args: List[String]): IO[ExitCode] = {
    val body = if (args.size >= 1) args(0) else s"""[{"table": "1", "name": "Pizza"}]"""
    val req = POST(body, uri("http://localhost:8080/orders"))
    val responseBody = BlazeClientBuilder[IO](global).resource.use(_.expect[String](req))
    responseBody.flatMap(resp => IO(println(resp))).as(ExitCode.Success)
  }
}

