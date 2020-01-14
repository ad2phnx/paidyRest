package dinis.anatoliy.paidyrestaurant

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import org.http4s.EntityEncoder
import org.http4s.circe._
import scala.collection.mutable.ListBuffer
import scala.util.Random
import cats.effect.IO

final case class OrderId(value: String = Random.alphanumeric.take(8).foldLeft("")((result, c) => result + c))
case class Order(table: String, name: String)
case class OrderWithTime(table: String, name: String, timeToMake: String)
case class OrderWithId(table: String, id: String, name: String, timeToMake: String)

trait OrderRepo {

  def orderWithTime(order: Order): OrderWithTime

  def addOrder(order: Order): IO[OrderId]
  def addOrders(orders: List[Order]): IO[List[OrderId]]
  def getOrder(id: OrderId): IO[Option[OrderWithId]]
  def deleteOrder(id: OrderId): IO[Either[String, Unit]]
  def updateOrder(id: OrderId, order: Order): IO[Either[String, Unit]]
  def getOrders(): IO[List[OrderWithId]]
  def getOrdersFromTable(table: String): IO[List[OrderWithId]]
}

object OrderRepo {

  class OrderImpl extends OrderRepo {

    import scala.collection.mutable.HashMap
    val storage = HashMap[OrderId, OrderWithTime]().empty

    override def orderWithTime(order: Order): OrderWithTime = {
      OrderWithTime(order.table, order.name, (5 + Random.nextInt(11)).toString)
    }

    override def addOrders(orders: List[Order]): IO[List[OrderId]] = IO {
      for (order <- orders)
        yield {
        val orderId = OrderId()
        storage.put(orderId, orderWithTime(order))
        orderId
      }
    }

    override def addOrder(order: Order): IO[OrderId] = IO {
      val orderId = OrderId()
      storage.put(orderId, orderWithTime(order))
      orderId
    }

    override def getOrder(id: OrderId): IO[Option[OrderWithId]] = IO {
      storage.get(id).map(order => OrderWithId(order.table, id.value, order.name, order.timeToMake))
    }

    override def deleteOrder(id: OrderId): IO[Either[String, Unit]] = 
      for {
        removedOrder <- IO(storage.remove(id))
        result = removedOrder.toRight(s"Order with ${id.value} not found").void
      } yield result

    override def updateOrder(id: OrderId, order: Order): IO[Either[String, Unit]] = {
      for {
        orderOpt <- getOrder(id)
        _ <- IO(orderOpt.toRight(s"Order with ${id.value} not found").void)
        updatedOrder = storage.put(id, orderWithTime(order)).toRight(s"Order with ${id.value} not found").void
      } yield updatedOrder
    }
    
    override def getOrders(): IO[List[OrderWithId]] = IO {
      storage.map {case (id, order) => OrderWithId(order.table, id.value, order.name, order.timeToMake)}.toList
    }

    override def getOrdersFromTable(table: String): IO[List[OrderWithId]] = IO {
      storage.filter((t) => t._2.table == table).map {case (id, order) => OrderWithId(order.table, id.value, order.name, order.timeToMake)}.toList
    }
  }
}
