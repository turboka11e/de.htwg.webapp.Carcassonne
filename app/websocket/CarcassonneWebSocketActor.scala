package websocket

import akka.actor.{Actor, ActorRef}
import de.htwg.se.Carcassonne.controller.controllerComponent.ControllerInterface
import de.htwg.se.Carcassonne.controller.controllerComponent.controllerBaseImpl.{FirstCard, GameOver, PlayfieldChanged}
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import utils.Utils.{last_col, last_row, manicanList}

import scala.swing.Reactor

class CarcassonneWebSocketActor(out: ActorRef, controller: ControllerInterface)
    extends Actor
    with Reactor {

  listenTo(controller)

  override def receive: Actor.Receive = { case msg: String =>
    val json = Json.parse(msg).as[JsObject]
    println(json)
    json.value map { case (s, value) =>
      s match {
        case "connect" => println("Connection: " + value.as[JsString].value)
        case "refresh" =>
          val row = value("row").as[JsNumber].value.toInt
          val col = value("col").as[JsNumber].value.toInt
          last_row = row
          last_col = col
          controller.placeCard(row, col)
          controller.placeAble()
      }
    }
  }

  def refreshGameBoard(): Unit = {
    val row = last_row
    val col = last_col

    out ! Json
      .obj(
        "refresh" -> Json.obj(
          "tileId" -> s"row${row}col${col}",
          "tile" -> views.html.Application
            ._tile(controller.getPlayfield, manicanList(controller), row, col)
            .toString,
          "freshCard" -> views.html.Application
            ._freshCard(controller.getPlayfield, manicanList(controller))
            .toString,
          "stats" -> views.html.Application
            ._stats(controller.getPlayfield)
            .toString
        )
      )
      .toString()
  }

  def sendNewGameEvent(): Unit = {
    out ! Json
      .obj(
        "newGame" -> "New Game has started"
      )
      .toString()
  }

  def sendGameOver(): Unit = {
    out ! Json.obj("gameOver" -> "gameOver").toString()
  }

  reactions += {
    case event: GameOver         => sendGameOver()
    case event: PlayfieldChanged => refreshGameBoard()
    case event: FirstCard        => sendNewGameEvent()
  }
}
