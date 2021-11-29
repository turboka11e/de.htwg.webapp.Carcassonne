package controllers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import de.htwg.se.Carcassonne.controller.controllerComponent.ControllerInterface
import play.api.Environment
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import utils.Utils.controller
import websocket.CarcassonneWebSocketActor

import javax.inject._

@Singleton
class GameController @Inject() (val controllerComponents: ControllerComponents)(
    implicit
    env: Environment,
    af: AssetsFinder,
    system: ActorSystem,
    mat: Materializer
) extends BaseController {

  val cardsIdsList = List(
    "D",
    "E",
    "G",
    "H",
    "I",
    "J",
    "K",
    "L",
    "N",
    "O",
    "R",
    "T",
    "U",
    "V",
    "C",
    "W"
  )

  def newGame(
      size: Int,
      p1: String,
      p2: String,
      p3: String,
      p4: String
  ): Action[AnyContent] =
    Action { _ =>
      controller.newGame()
      controller.createGrid(size)
      for (player <- List(p1, p2, p3, p4)) {
        if (player.trim.nonEmpty) controller.addPlayer(player)
      }
      controller.firstCard()
      Redirect(routes.GameController.gameBoard()).withSession("username" -> p1)
    }

  def hardNewGame(): Action[AnyContent] = Action {
    controller.newGame()
    Redirect(routes.HomeController.index()).withNewSession
  }

  def joinGame(username: String): Action[AnyContent] = Action { _ =>
    Redirect(routes.GameController.gameBoard())
      .withSession("username" -> username)
  }

  def gameBoard(): Action[AnyContent] = Action { request =>
    request.session.get("username") match {
      case None => Redirect(routes.HomeController.index()).withNewSession
      case Some(username) =>
        if (controller.getPlayfield.gameState < 2) {
          Redirect(routes.HomeController.index()).withNewSession
        } else {
          controller.getPlayfield.players.find(player =>
            player.name == username
          ) match {
            case None => Redirect(routes.HomeController.index()).withNewSession
            case Some(player) => Ok(views.html.Application.gameBoard())
          }
        }
    }
  }

  def socket: WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      CarcassonneWebSocketFactory.create(
        out,
        controller,
        request.session.get("username").getOrElse("")
      )
    }
  }

  object CarcassonneWebSocketFactory {
    def create(
        out: ActorRef,
        c: ControllerInterface,
        username: String
    ): Props = {
      Props(new CarcassonneWebSocketActor(out, c, username, cardsIdsList)(af))
    }
  }

}
