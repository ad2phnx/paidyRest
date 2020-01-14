package dinis.anatoliy.paidyrestaurant

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult
import io.circe.Json
import io.circe.Decoder
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.Uri
import org.http4s.Uri.uri

class OrdersSpec extends org.specs2.mutable.Specification {

  "Orders" >> {
    "post order returns 201" >> {
      postOrderReturns201()
    }
    "post order returns order id" >> {
      postOrderReturnsOrderId()
    }
	"post orders returns 201" >> {
	  postOrdersReturns201()
	}
	"post orders returns list order ids" >> {
	  postOrdersReturnsOrderIds()
	}
	"get empty order returns 404" >> {
	  getEmptyOrderReturns404()
	}
	"get empty order returns empty list" >> {
	  getEmptyOrderReturnsEmptyList()
	}
	"get order returns 200" >> {
	  getOrderReturns200()
	}
	"get order returns order" >> {
	  getOrderReturnsOrder()
	}
	"get orders returns 200" >> {
	  getEmptyOrdersReturns200()
	  getOrdersReturns200()
	}
	"get orders returns orders" >> {
	  getEmptyOrdersReturnsEmptyList()
	  getOrdersReturnsOrders()
	}
  }

  private[this] val retPostOrder: Response[IO] = {
    val body = s"""{"table": "1", "name": "pizza"}"""
    val post = Request[IO](Method.POST, uri"/order").withBody(body).unsafeRunSync()
    val orders = new OrderRepo.OrderImpl
    Routes.routes(orders).orNotFound(post).unsafeRunSync()
  }

  private[this] val retPostOrders: Response[IO] = {
    val body = s"""[{"table": "1", "name": "pizza"},{"table": "2", "name": "hamburger"}]"""
    val post = Request[IO](Method.POST, uri"/orders").withBody(body).unsafeRunSync()
    val orders = new OrderRepo.OrderImpl
    Routes.routes(orders).orNotFound(post).unsafeRunSync()
  }

  private[this] val retGetEmptyOrder: Response[IO] = {
	val getOrder = Request[IO](Method.GET, uri"/order/abcd1234")
	val order = new OrderRepo.OrderImpl
	Routes.routes(order).orNotFound(getOrder).unsafeRunSync()
  }

  private[this] val retGetOrder: Response[IO] = {
	val order = new OrderRepo.OrderImpl
    val body = s"""{"table": "1", "name": "pizza"}"""
    val post = Request[IO](Method.POST, uri"/order").withBody(body).unsafeRunSync()
    var res = Routes.routes(order).orNotFound(post).unsafeRunSync()
	var idJson = res.as[Json].unsafeRunSync()
	val getOrder = Request[IO](Method.GET, uri"/orders")
	Routes.routes(order).orNotFound(getOrder).unsafeRunSync()
  }

  private[this] val retGetEmptyOrders: Response[IO] = {
	val getOrders = Request[IO](Method.GET, uri"/orders")
	val orders = new OrderRepo.OrderImpl
	Routes.routes(orders).orNotFound(getOrders).unsafeRunSync()
  }

  private[this] val retGetOrders: Response[IO] = {
	val orders = new OrderRepo.OrderImpl
    val body = s"""[{"table": "1", "name": "pizza"},{"table": "2", "name": "hamburger"}]"""
    val post = Request[IO](Method.POST, uri"/orders").withBody(body).unsafeRunSync()
    Routes.routes(orders).orNotFound(post).unsafeRunSync()
	val getOrders = Request[IO](Method.GET, uri"/orders")
	Routes.routes(orders).orNotFound(getOrders).unsafeRunSync()
  }

  private[this] def postOrderReturns201(): MatchResult[Status] =
    retPostOrder.status must beEqualTo(Status.Created)

  private[this] def postOrderReturnsOrderId(): MatchResult[String] =
    retPostOrder.as[String].unsafeRunSync() must beMatching("\\{\"id\":\"[a-zA-Z0-9]{8}\"\\}")

  private[this] def postOrdersReturns201(): MatchResult[Status] =
	retPostOrders.status must beEqualTo(Status.Created)

  private[this] def postOrdersReturnsOrderIds(): MatchResult[String] =
    retPostOrders.as[String].unsafeRunSync() must beMatching("\\[\\{\"id\":\"[a-zA-Z0-9]{8}\"\\},\\{\"id\":\"\\w{8}\"\\}\\]")

  private[this] def getEmptyOrderReturns404(): MatchResult[Status] =
	retGetEmptyOrder.status must beEqualTo(Status.NotFound)

  private[this] def getEmptyOrderReturnsEmptyList(): MatchResult[String] =
	retGetEmptyOrder.as[String].unsafeRunSync() must beMatching("")
  
  private[this] def getOrderReturns200(): MatchResult[Status] =
    retGetOrder.status must beEqualTo(Status.Ok)

  private[this] def getOrderReturnsOrder(): MatchResult[String] =
	retGetOrder.as[String].unsafeRunSync() must beMatching("\\[\\{\"table\":\"1\",\"id\":\"\\w{8}\",\"name\":\"pizza\",\"timeToMake\":\"\\d+\"\\}\\]")
  

  private[this] def getEmptyOrdersReturns200(): MatchResult[Status] =
	retGetEmptyOrders.status must beEqualTo(Status.Ok)

  private[this] def getEmptyOrdersReturnsEmptyList(): MatchResult[String] =
	retGetEmptyOrders.as[String].unsafeRunSync() must beMatching("\\[\\]")
  
  private[this] def getOrdersReturns200(): MatchResult[Status] =
    retGetOrders.status must beEqualTo(Status.Ok)

  private[this] def getOrdersReturnsOrders(): MatchResult[String] =
	retGetOrders.as[String].unsafeRunSync() must beMatching("\\[\\{\"table\":\"\\d+\",\"id\":\"\\w{8}\",\"name\":\"\\w+\",\"timeToMake\":\"\\d+\"\\},\\{\"table\":\"\\d+\",\"id\":\"\\w{8}\",\"name\":\"\\w+\",\"timeToMake\":\"\\d+\"\\}\\]")
  
}
