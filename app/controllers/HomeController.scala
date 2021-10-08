package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import de.htwg.se.Carcassonne._
import com.google.inject.{Guice, Injector}
import de.htwg.se.Carcassonne.aview.gui.StartGUI
import de.htwg.se.Carcassonne.aview.tui._
import de.htwg.se.Carcassonne.controller.controllerComponent.ControllerInterface
import de.htwg.se.Carcassonne.model.playfieldComponent.PlayfieldInterface

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  val injector: Injector = Guice.createInjector(new CarcassonneModule)
  val controller: ControllerInterface = injector.getInstance(classOf[ControllerInterface])
  val tui = new TUI(controller)

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def newGame = Action { request =>
    controller.newGame()
    Ok(views.html.newGame())
  }

  def createGrid(size: Int) = Action { implicit request =>
    controller.createGrid(size.toInt)
    Ok(views.html.Application.addPlayer(controller.getPlayfield.getPlayers))
  }

  def addPlayer(player: String) = Action {
    controller.addPlayer(player)
    Ok(views.html.Application.addPlayer(controller.getPlayfield.getPlayers))
  }

  def startGame() = Action {
    controller.firstCard()
    Ok(views.html.Application.gameBoard(controller.getPlayfield))
  }

  def gameBoard() = Action {
    Ok(views.html.Application.gameBoard(controller.getPlayfield))
  }

  def placeCard(row: Int, col: Int, x: Int, y: Int) = Action {
    controller.placeCard(row, col)
    Ok(views.html.Application.gameBoard(controller.getPlayfield))
  }

  def rotate(dir: String) = Action {
    if (dir.equals("Left")) {
      controller.rotateLeft
    }
    if (dir.equals("Right")) {
      controller.rotateRight
    }
    Ok(views.html.Application.gameBoard(controller.getPlayfield))
  }

}
