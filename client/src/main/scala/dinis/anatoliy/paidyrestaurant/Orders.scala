package dinis.anatoliy.paidyrestaurant

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import org.http4s.EntityEncoder
import org.http4s.circe._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.util.Random
import cats.effect.IO

// OrderId model class to create an alphanumeric 8 character long ID for an order, used to identify when
// delete/modifying orders
final case class OrderId(value: String = Random.alphanumeric.take(8).foldLeft("")((result, c) => result + c))
// Order model class representing how orders come in with requests, an order is a dish name and belongs to a table
case class Order(table: String, name: String)
// Order are stored in a hashmap and have a time to make field representing minutes a dish will take to make
case class OrderWithTime(table: String, name: String, timeToMake: String)
// When orders are requested for viewing they will be shown with table belonging to, id, name and time to make
case class OrderWithId(table: String, id: String, name: String, timeToMake: String)


// Describe an orders functionality
trait OrderRepo {

  // Helper function to create random time to make
  def orderWithTime(order: Order): OrderWithTime

  // Functions used in api calls
  def addOrder(order: Order): IO[OrderId]
  def addOrders(orders: List[Order]): IO[List[OrderId]]
  def getOrder(id: OrderId): IO[Option[OrderWithId]]
  def deleteOrder(id: OrderId): IO[Either[String, Unit]]
  def updateOrder(id: OrderId, order: Order): IO[Either[String, Unit]]
  def getOrders(): IO[List[OrderWithId]]
  def getOrdersFromTable(table: String): IO[List[OrderWithId]]
}

// An order repository object holding an order implementation class
object OrderRepo {

  // The actuall implementation class with storage and function for CRUD operations
  // used in the order routes
  class OrderImpl extends OrderRepo {

    // Store orders in HashMap by random order id
    val storage = HashMap[OrderId, OrderWithTime]().empty

    // Take a new order and create a random time between 5 to 15 minutes that it will take to make
    override def orderWithTime(order: Order): OrderWithTime = {
      OrderWithTime(order.table, order.name, (5 + Random.nextInt(11)).toString)
    }

    // Add a list of order to the storage hashmap
    override def addOrders(orders: List[Order]): IO[List[OrderId]] = IO {
      for (order <- orders)
        yield {
        val orderId = OrderId()
        storage.put(orderId, orderWithTime(order))
        orderId
      }
    }

    // Add a single order to storage container
    override def addOrder(order: Order): IO[OrderId] = IO {
      val orderId = OrderId()
      storage.put(orderId, orderWithTime(order))
      orderId
    }

    // Find an order in storage hashmap by an OrderId
    override def getOrder(id: OrderId): IO[Option[OrderWithId]] = IO {
      storage.get(id).map(order => OrderWithId(order.table, id.value, order.name, order.timeToMake))
    }

    // Delete an order from the storage hashmap by OrderId
    override def deleteOrder(id: OrderId): IO[Either[String, Unit]] = 
      for {
        removedOrder <- IO(storage.remove(id))
        result = removedOrder.toRight(s"Order with ${id.value} not found").void
      } yield result

    // Update an order properties (table, name) base on OrderId and set new time
    override def updateOrder(id: OrderId, order: Order): IO[Either[String, Unit]] = {
      for {
        orderOpt <- getOrder(id)
        _ <- IO(orderOpt.toRight(s"Order with ${id.value} not found").void)
        updatedOrder = storage.put(id, orderWithTime(order)).toRight(s"Order with ${id.value} not found").void
      } yield updatedOrder
    }
    
    // Get a list of all orders for all tables from storage
    override def getOrders(): IO[List[OrderWithId]] = IO {
      storage.map {case (id, order) => OrderWithId(order.table, id.value, order.name, order.timeToMake)}.toList
    }

    // Get a list of all orders for a particular table
    override def getOrdersFromTable(table: String): IO[List[OrderWithId]] = IO {
      storage.filter((t) => t._2.table == table).map {case (id, order) => OrderWithId(order.table, id.value, order.name, order.timeToMake)}.toList
    }
  }
}
