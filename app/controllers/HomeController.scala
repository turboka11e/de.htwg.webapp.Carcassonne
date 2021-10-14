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
    Ok(views.html.Application.gameBoard(controller.getPlayfield, manicanList()))
  }

  def gameBoard() = Action {
    val cards = List("D", "E", "G", "H", "I", "J", "K", "L", "N", "O", "R", "T", "U", "V", "C", "W")
    
    Ok(views.html.Application.gameBoard(controller.getPlayfield, manicanList()))
  }

  def manicanList(): List[(String, String)] = {
    val freshCardAreas = controller.getPlayfield.getCurrentFreshCard.getCard.getAreas
    val dirCombi = List("north", "south", "west", "east")

    var manicanList: List[(String, String)] = Nil

    if (!freshCardAreas.exists(p => p.getPlayer != -1)) {
        for (x <- freshCardAreas.indices) {
            val selectDir: String = dirCombi.find(p => p.startsWith(freshCardAreas(x).getCorners.head.toString)).get
            freshCardAreas(x).getValue match {
            case 'c' => manicanList = (selectDir, "manicanEmpty") :: manicanList
            case 'r' => manicanList = (selectDir, "manicanEmpty") :: manicanList
            case 'g' =>
          }
        }
    } else {
      val dir = controller.getPlayfield.getCurrentFreshCard.getCard.getAreas.find(p => p.getPlayer != -1).get.getCorners.head
      val combi = dirCombi.find(p => p.startsWith(dir.toString)).get
      val playerManican = "manican" + controller.getPlayfield.getIsOn
      manicanList = (combi, playerManican) :: manicanList
    }
    manicanList
  }

  def selectArea(dir: String, x: Int, y: Int) = Action {

    dir match {
      case "north" => putManican('n')
      case "south" => putManican('s')
      case "east" => putManican('e')
      case "west" => putManican('w')
    }
    
    Ok(views.html.Application.gameBoard(controller.getPlayfield, manicanList()))
  }

  def putManican(dir: Char): Unit = {
        val area = controller.getPlayfield.getCurrentFreshCard.getCard.getAreaLookingFrom(dir)
        val index = controller.getPlayfield.getCurrentFreshCard.getCard.getAreas.indexWhere(p => p.eq(area))
        controller.selectArea(index)
    }

  def placeCard(row: Int, col: Int, x: Int, y: Int) = Action {
    controller.placeCard(row, col)
    Ok(views.html.Application.gameBoard(controller.getPlayfield, manicanList()))
  }

  def rotate(dir: String) = Action {
    if (dir.equals("Left")) {
      controller.rotateLeft
    }
    if (dir.equals("Right")) {
      controller.rotateRight
    }
    Ok(views.html.Application.gameBoard(controller.getPlayfield, manicanList()))
  }

  def rules() = Action {
    Ok(views.html.rules())
  }

}
