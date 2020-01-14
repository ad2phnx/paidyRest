# Interview Coding Test for Paidy
##### Author: Anatoliy Dinis Date: January 14, 2020
---

The `paidyRest` application is a simple restaurant order management application consisting of two main components; a server to handle all restaurant orders and a client to that can talk to the server to view, add, remove and update orders.

The server runs indefinitely once started and runs default on `localhost:8080`

The client is only used to POST a Json array of Order objects which are passed in as a string argument. To make any other requests, curl is used.

## Getting Started

### Prerequisites

* `jp` - There is a `bash` script called `run5Clients.sh` in the main directory that will create create 5 clients and pass in a list of **JSON** order items to them from the `generated#.json` files. This script uses `jq` - Command-line JSON processor which can be installed on most linux distros to correctly parse the json files and pass the json as a string to the client(s)

* `sbt` - To copmile and run the source `sbt` is used with `sbt run` to run and `sbt test` for unit tests.

These instructions will get the project installed and ready to test or run using either `sbt` or `scala`.

After unzipping the project the directory structure is as follows:

```
praidyRest
|   generated1.json         # List of {table: TableName, name: DishName} json objects
|   generated2.json
|   generated3.json
|   generated4.json
|   generated5.json
|   jsonOrderGenerator.js   # Code used to generate json files above from json-generator.com
|   README.md               # This readme file
|   run5Clients.sh          # Bash script to run 5 clients and POST generated#.json lists
|___client                  # client Scala project
|   |   build.sbt           # SBT build file containing needed dependencies and build settings
|   |___project             # client Project folder with project properties
|   |   |   ...
|   |___src                 # client source code files for main and test
|   |   |   ...
|   |___target              # client output build folder
|       |___scala-212       # scala version built against
|           |   paidy-restaurant-assembly-0.0.1-SNAPSHOT.jar
|                           # Assembled JAR file with dependencies
|___server                  # server Scala project
|   |   build.sbt           # SBT build file containing needed dependencies and build settings
|   |___project             # client Project folder with project properties
|   |   |   ...
|   |___src                 # client source code files for main and test
|   |   |   ...
|   |___target              # client output build folder
|       |___scala-212       # scala version built against
|           |   paidy-restaurant-assembly-0.0.1-SNAPSHOT.jar
|                           # Assembled JAR file with dependencies

```

There are two prebuilt **JAR** files that have been assembled with the `assembly` plugin for `sbt` under `client/target/scala-2.12/paidy-restaurant-assembly-0.0.1-SNAPSHOT.jar` and `server/target/scala-2.12/paidy-restaurant-assembly-0.0.1-SNAPSHOT.jar` for the `client` and `server` respectively which can be invoked with `scala ${JAR_FILE}`.

### API

To create an `Order` a Json object of the format

```json
{"table": tableName, "name": dishName}
```

is POSTed to the server which in turn appends a `"timeToMake": timeInMinutes` as well as a randomly generated order id as `"id:" 8CharAlphanumeric` to the order item.

To POST multiple orders the `Order` objects above should be put into a standard Json list.

After POSTing an order the response will return a Json object with the generated id of the order

```json
{"id": orderId}
```

If POSTing multiple orders then the response will respond with a Json list of the created order ids.

When doing `GET` calls the server will either return an order in the form

```json
{"table": tableName, "id": orderId, "name": dishName, "timeToMake": timeInMinutes}
```

The following calls are used to manipulate orders with the `server` running.

* `GET -> "/orders"`                - Get a json list of all orders for all tables
* `GET -> "/order/" + orderId`      - Get an individual order by the `orderId`
* `GET -> "/table/" + tableName + "/orders"`    - Get all orders for a certain `tableName`
* `POST -> "/order"`                - Post an order as a Json object in the data section
* `PUT -> "/order/" + orderId`      - Update and order by the `orderId`
* `DELETE -> "/order/" + orderId`   - Delete an order by the `orderId`

When doing a **PUT** or **DELETE** the response will either be OK or a NotFound with an error message.

### Testing

To run the tests, `cd` into the `server` directory and either invoke `sbt` and then `test` in the sbt console or directly invoke `sbt test` from the terminal.

Since the client is fairly simple and uses the same `Order` objects as the server, tests have only been added to the `server` project. The following tests are defined in `OrderSpec.scala` in the `server` source directory.

* `PostOrderReturns201`     - Tests when posting an order a correct Status of 201 (Created) is returned
* `PostOrderReturnsOrderId` - Make sure when posting an order an OrderId is returned as Json
* `PostOrdersReturn201`     - Tests when posting a multiple orders a correct Status of 201 (Created) is returned
* `PostOrdersReturnsOrderIds`    Make sure when posting multiple orders, a list of created OrderIds is returned
* `GetEmptyOrderReturns404` - When trying to get an order that doesn't exist, make sure 404 is returned
* `GetEmptyOrderReturnsEmptyString`   - Return empty string when trying to get order that doesn't exist
* `GetOrderReturns200`      - Test getting an order after putting an order returns Status of 200 (Ok)
* `GetOrderReturnsOrder`    - After posting an order, make sure order exists in orders
* `GetEmptyOrdersReturns200`   - Make sure when getting all orders and none exist we still get 200 Status
* `GetEmptyOrdersReturnsEmptyList`  - When querying for all orders and none exist should return empty list
* `GetOrdersReturns200`     - Put some orders and request to get them should return status 200 (Ok)
* `GetOrdersReturnsOrders`  - After putting orders and getting all orders, compare tables and dish names match

There are more tests that need to be added for the other REST operations of **DELETE** and **UPDATE** orders which haven't been added because when getting or updating individual orders the created OrderId needs to be appended to the **URI**. This has proven to be difficult due to creating dynamic URIs is challenging with a `Either`  type parsed Json from a POST response.


In this case, manual tests were run to test **DELETE** and **UPDATE** capabilities as well as **PUT** and **GET**.

* First the `server` is started by calling `sbt run` from the `server` directory. Then in another terminal

   `curl localhost:8080/orders`

  is run to make sure no orders are yet in the system. Then to fill the server with orders either the script to run 5 clients is run

    `./run5Clients.sh`

  from the main directory, or a json string of json array of `Order` objects is passed to the client with

    `sbt run '[{"table": "1", "name": "pizza"}]'`

  or

    `scala target/scala-2.12/paidy-restaurant-assembly-0.0.1-SNAPSHOT.jar '[{"table": "1", "name": "pizza"}]'`

  or just a curl command is used to directly make requests to the server

    `curl -X POST -H "Content-Type: application/json" localhost:8080/orders -d '[{"table": "1", "name": "pizza"}]'`

* Then more items can be added or curl commands can be used to test deleting and updated orders from the server

    `curl -X DELETE -H "Content-Type: application/json" localhost:8080/order/${orderId}`

### Deployment

If this project was to be meant for serious production then changes such as using a database, converting the requests to pure functional scala standards, using https, guis, limiting request paths, etc. would need to be implemented.

### Acknowledgements

http4s example github repositories
CRUD for Scala tutorials
