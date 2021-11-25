package websocket

import akka.actor.{Actor, ActorRef}
import controllers.AssetsFinder
import de.htwg.se.Carcassonne.controller.controllerComponent.ControllerInterface
import de.htwg.se.Carcassonne.controller.controllerComponent.controllerBaseImpl.{
  FirstCard,
  GameOver,
  PlayfieldChanged
}
import play.api.libs.json.{
  JsArray,
  JsBoolean,
  JsNumber,
  JsObject,
  JsString,
  Json
}
import utils.Utils
import utils.Utils.{last_col, last_row, manicanList}

import scala.swing.Reactor

class CarcassonneWebSocketActor(
    out: ActorRef,
    controller: ControllerInterface,
    username: String,
    cardsIdsList: List[String]
)(
    af: AssetsFinder
) extends Actor
    with Reactor {

  listenTo(controller)

  override def receive: Actor.Receive = { case msg: String =>
    val json = Json.parse(msg).as[JsObject]
    println(json)
    json.value map { case (s, value) =>
      s match {
        case "connect" => {
          println("Connection: " + value.as[JsString].value)
          sendSessionUserName()
          sendStats()
          sendFreshCard()
          sendGrid()
        }
        case "refresh" =>
          val row = value("row").as[JsNumber].value.toInt
          val col = value("col").as[JsNumber].value.toInt
          last_row = row
          last_col = col
          controller.placeCard(row, col)
          controller.placeAble()
          sendFreshCard()
          sendStats()
          sendGrid()
        case "rotate" =>
          value.as[JsString].value match {
            case "Right" => controller.rotateRight()
            case "Left"  => controller.rotateLeft()
          }
        case "manican" =>
          value.as[JsString].value match {
            case "north" => putManican('n')
            case "south" => putManican('s')
            case "east"  => putManican('e')
            case "west"  => putManican('w')
          }
      }
    }
  }

  def putManican(dir: Char): Unit = {
    val area = controller.getPlayfield.freshCard.card.getAreaLookingFrom(dir)
    val index =
      controller.getPlayfield.freshCard.card.areas.indexWhere(p => p.eq(area))
    controller.selectArea(index)
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

  def sendSessionUserName(): Unit = {
    out ! Json
      .obj(
        "username" -> username
      )
      .toString()
  }

  def sendFreshCard(): Unit = {
    val freshCardId = controller.getPlayfield.freshCard.card.id
    out ! Json
      .obj(
        "freshCard" -> Json.obj(
          "src" -> af.path(
            "images/media/" + cardsIdsList(freshCardId._1) + ".png"
          ),
          "rotation" -> JsNumber(freshCardId._2 * 90),
          "manicans" -> Json.toJson(
            Utils.manicanList(controller) map (manican => {
              Json.obj(
                "dir" -> manican._1,
                "path" -> af.path("images/media/" + manican._2 + ".png"),
                "disabled" -> JsBoolean(!(manican._2 equals "manicanEmpty"))
              )
            })
          ),
          "manicansShow" -> JsBoolean(true)
        )
      )
      .toString()
  }

  def sendStats(): Unit = {
    val colors = List("Blue", "Red", "Gold", "Green");
    val isOn = controller.getPlayfield.isOn

    out ! Json
      .obj(
        "stats" -> Json.obj(
          "players" -> Json.toJson(
            controller.getPlayfield.players.zipWithIndex map {
              case (player, index) =>
                Json.obj(
                  "player" -> player.toString,
                  "color" -> colors(index),
                  "isOn" -> JsBoolean(index == isOn)
                )
            }
          )
        )
      )
      .toString()
  }

  def sendGrid(): Unit = {
    val gridSize = controller.getPlayfield.grid.size

    val grid = for {
      col: Int <- 0 until gridSize
    } yield {
      val arow = for (row <- 0 until gridSize) yield {
        val card = controller.getPlayfield.grid.card(row, col);

        val areasManicans: List[JsObject] = for {
          area <- card.areas
          if area.player != -1
        } yield {
          Json.obj(
            "dir" -> List("north", "south", "west", "east")
              .find(p => p.startsWith(area.corners.head.toString))
              .get,
            "src" -> af.path("images/media/manican" + area.player + ".png")
          )
        }

        val imgSrc = if (card.id._1 == -1) {
          af.path("images/media/Empty.png")
        } else {
          af.path("images/media/" + cardsIdsList(card.id._1) + ".png")
        }

        Json.obj(
          "row" -> JsNumber(row),
          "col" -> JsNumber(col),
          "src" -> imgSrc,
          "manicans" -> JsArray(areasManicans),
          "empty" -> JsBoolean(card.id._1 == -1),
          "rotation" -> JsNumber(card.id._2 * 90)
        )
      }
      Json.obj(
        "cols" -> JsArray(arow)
      )
    }

    out ! Json
      .obj(
        "grid" -> Json.obj(
          "rows" -> JsArray(grid)
        )
      )
      .toString()
  }

  reactions += {
    case event: GameOver => sendGameOver()
    case event: PlayfieldChanged =>
      sendStats()
      sendFreshCard()
      sendGrid()
    case event: FirstCard => sendNewGameEvent()
  }
}
