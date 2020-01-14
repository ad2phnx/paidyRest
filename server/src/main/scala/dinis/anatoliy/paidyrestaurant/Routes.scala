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

// Routes object describing all possible http routes we can use
// to add, remove, update, and view orders
object Routes {

  // Helper function to display json error messages
  private def errorBody(message: String) = Json.obj(
    ("message", Json.fromString(message))
  )

  // Routes function using the order repository
  def routes(orderRepo: OrderRepo): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO]{}
    import dsl._

    // Describe each possible route
    HttpRoutes.of[IO] {

      // Plain get all orders
      case _ @ GET -> Root / "orders" =>
        orderRepo.getOrders().flatMap(orders => Ok(orders))

      // Get orders belonging to a certain table ex. /table/1/orders
      case _ @ GET -> Root / "table" / table / "orders" =>
        orderRepo.getOrdersFromTable(table).flatMap(orders => Ok(orders))

      // Post a list of Orders to add to storage as a json array of {table: tableName, name: dishName} values
      // Responds with a list of created orders ids
      case req @ POST -> Root / "orders" =>
        req.decode[List[Order]] { orders =>
          orderRepo.addOrders(orders) flatMap(ids =>
              //Ok(ids)
              Created(ids.map(id =>
                  Json.obj(("id", Json.fromString(id.value))))
              )
          )
        }

      // Post a single order as {table: tableName, name: dishName} json object
      // Responds with a created order id
      case req @ POST -> Root / "order" =>
        req.decode[Order] { order =>
          orderRepo.addOrder(order) flatMap(id =>
              Created(Json.obj(("id", Json.fromString(id.value))))
          )
        }

      // Get an order using the OrderId
      case req @ GET -> Root / "order" / id =>
        orderRepo.getOrder(OrderId(id)) flatMap {
          case None => NotFound()
          case Some(order) => Ok(order)
        }

      // Update an order using OrderId and either return Ok for success
      // or NotFound with an error message for failure
      case req @ PUT -> Root / "order" / id =>
        req.decode[Order] { order =>
          orderRepo.updateOrder(OrderId(id), order) flatMap {
            case Left(message) => NotFound(errorBody(message))
            case Right(_) => Ok()
          }
        }

      // Delete an order using OrderId and either return Ok for success
      // or NotFound with an error message for failure
      case _ @ DELETE -> Root / "order" / id =>
        orderRepo.deleteOrder(OrderId(id)) flatMap {
          case Left(message) => NotFound(errorBody(message))
          case Right(_) => Ok()
        }
    }
  }
}
