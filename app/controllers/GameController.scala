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
      Props(new CarcassonneWebSocketActor(out, c, username, cardsIdsList)(af))
    }
  }

}
