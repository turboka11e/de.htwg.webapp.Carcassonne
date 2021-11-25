package controllers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.google.inject.{Guice, Injector}
import de.htwg.se.Carcassonne._
import de.htwg.se.Carcassonne.aview.tui._
import de.htwg.se.Carcassonne.controller.controllerComponent.ControllerInterface
import play.api.Environment
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import utils.Utils.manicanList
import websocket.CarcassonneWebSocketActor

import javax.inject._
import scala.concurrent.Future

@Singleton
class GameController @Inject() (val controllerComponents: ControllerComponents)(
    implicit
    env: Environment,
    af: AssetsFinder,
    system: ActorSystem,
    mat: Materializer
) extends BaseController {

  val injector: Injector = Guice.createInjector(new CarcassonneModule)
  val controller: ControllerInterface =
    injector.getInstance(classOf[ControllerInterface])
  val tui = new TUI(controller)

  val cardsIdsList = List("D", "E" , "G" , "H" , "I"
    , "J" , "K" , "L" , "N" , "O" , "R" , "T" , "U" , "V" , "C" , "W" )

  def newGame(
      size: Int,
      p1: String,
      p2: String,
      p3: String,
      p4: String
  ): Action[AnyContent] =
    Action { request =>
      controller.newGame()
      controller.createGrid(size)
      controller.firstCard()

      for (player <- List(p1, p2, p3, p4)) {
        if (player.trim.nonEmpty) controller.addPlayer(player)
      }

      Redirect(routes.GameController.gameBoard()).withSession("username" -> p1)
    }

  def gameBoard(): Action[AnyContent] = Action { request =>
    if (controller.getPlayfield.gameState < 2) {
      Ok(views.html.index())
    } else {
      Ok(
        views.html.Application
          .gameBoard(controller.getPlayfield, manicanList(controller))
      )
    }
  }

  def selectArea(): Action[AnyContent] = Action { implicit request =>
    val json = request.body.asJson
    json
      .map { json =>
        val dir = (json \ "dir").as[String];
        println("Selected Manican: " + dir);
        dir match {
          case "north" => putManican('n')
          case "south" => putManican('s')
          case "east"  => putManican('e')
          case "west"  => putManican('w')
        }
        Ok(
          views.html.Application
            ._freshCard(controller.getPlayfield, manicanList(controller))
        ).as(HTML)
      }
      .getOrElse {
        InternalServerError("Error")
      }
  }

  def putManican(dir: Char): Unit = {
    val area = controller.getPlayfield.freshCard.card.getAreaLookingFrom(dir)
    val index =
      controller.getPlayfield.freshCard.card.areas.indexWhere(p => p.eq(area))
    controller.selectArea(index)
  }

  def placeCard(): Action[AnyContent] = Action { implicit request =>
    val json = request.body.asJson
    json
      .map { json =>
        val row = (json \ "row").as[Int];
        val col = (json \ "col").as[Int];
        println("Updated: Row " + row + " Col " + col);
        controller.placeCard(row, col)
        controller.placeAble()

        val respond = Json.obj(
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

        Ok(respond).as(JSON)
      }
      .getOrElse {
        InternalServerError("Error")
      }
  }

  def rotate(): Action[AnyContent] = Action { implicit request =>
    val json = request.body.asJson

    json
      .map { json =>
        val dir = (json \ "dir").as[String];
        println(dir);
        if (dir.equals("Left")) {
          controller.rotateLeft()
        }
        if (dir.equals("Right")) {
          controller.rotateRight()
        }
        Ok(
          views.html.Application
            ._freshCard(controller.getPlayfield, manicanList(controller))
        ).as(HTML)
      }
      .getOrElse {
        println(request.body)
        Ok("error")
      }
  }

  def playfield(): Action[AnyContent] = Action { implicit request =>
    val username = request.session.get("username")

    val freshCardId = controller.getPlayfield.freshCard.card.id._1

    val path = af.path("images/media/" + cardsIdsList(freshCardId) + ".png")

    val json = Json.obj(
      "path" -> path,
      "username" -> username.get,
    )

    Ok(json).as(JSON)
  }

  def socket: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(request.session.get("username") match {
      case None => Left(Forbidden)
      case Some(username) => Right(
        ActorFlow.actorRef { out =>
          CarcassonneWebSocketFactory.create(out, controller, username)
        })
    })
  }

  object CarcassonneWebSocketFactory {
    def create(out: ActorRef, c: ControllerInterface, username: String): Props = {
      Props(new CarcassonneWebSocketActor(out, c, username))
    }
  }

}
