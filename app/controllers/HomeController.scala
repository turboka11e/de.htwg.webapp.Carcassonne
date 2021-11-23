package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import de.htwg.se.Carcassonne._
import com.google.inject.{Guice, Injector}
import de.htwg.se.Carcassonne.aview.gui.StartGUI
import de.htwg.se.Carcassonne.aview.tui._
import de.htwg.se.Carcassonne.controller.controllerComponent.ControllerInterface
import de.htwg.se.Carcassonne.model.playfieldComponent.PlayfieldInterface

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  val injector: Injector = Guice.createInjector(new CarcassonneModule)
  val controller: ControllerInterface =
    injector.getInstance(classOf[ControllerInterface])
  val tui = new TUI(controller)

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be
    * called when the application receives a `GET` request with a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def newGame(size: Int, p1: String, p2: String, p3: String, p4: String) = Action { request =>
    controller.newGame()
    controller.createGrid(size)
    controller.firstCard()

    for (player <- List(p1, p2, p3, p4)) {
      if(!player.trim.isEmpty) controller.addPlayer(player)
    }

    Redirect(routes.HomeController.gameBoard())
  }

  def gameBoard() = Action {
    if (controller.getPlayfield.gameState < 2) {
      Ok(views.html.index())
    } else {  
      Ok(views.html.Application.gameBoard(controller.getPlayfield, manicanList()))
    }
  }

  def manicanList(): List[(String, String)] = {
    val freshCardAreas = controller.getPlayfield.freshCard.card.areas
    val dirCombi = List("north", "south", "west", "east")

    var manicanList: List[(String, String)] = Nil

    if (!freshCardAreas.exists(p => p.player != -1)) {
      for (x <- freshCardAreas.indices) {
        val selectDir: String = dirCombi
          .find(p => p.startsWith(freshCardAreas(x).corners.head.toString))
          .get
        freshCardAreas(x).value match {
          case 'c' => manicanList = (selectDir, "manicanEmpty") :: manicanList
          case 'r' => manicanList = (selectDir, "manicanEmpty") :: manicanList
          case 'g' =>
        }
      }
    } else {
      val dir = controller.getPlayfield.freshCard.card.areas
        .find(p => p.player != -1)
        .get
        .corners
        .head
      val combi = dirCombi.find(p => p.startsWith(dir.toString)).get
      val playerManican = "manican" + controller.getPlayfield.isOn
      manicanList = (combi, playerManican) :: manicanList
    }
    manicanList
  }

  def selectArea() = Action { implicit request =>
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
            ._freshCard(controller.getPlayfield, manicanList())
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

  def placeCard() = Action { implicit request =>
    val json = request.body.asJson
    json
      .map { json =>
        val row = (json \ "row").as[Int];
        val col = (json \ "col").as[Int];
        println("Updated: Row " + row + " Col " + col);
        controller.placeCard(row, col)

        var respond = Json.obj(
          "tile" -> views.html.Application._tile(controller.getPlayfield, manicanList(), row, col).toString,
          "freshCard" -> views.html.Application._freshCard(controller.getPlayfield, manicanList()).toString,
          "stats" -> views.html.Application._stats(controller.getPlayfield).toString,
        )

        Ok(respond).as(JSON)
      }
      .getOrElse {
        InternalServerError("Error")
      }
  }

  def rotate() = Action { implicit request =>
    val json = request.body.asJson

    json
      .map { json =>
        val dir = (json \ "dir").as[String];
        println(dir);
        if (dir.equals("Left")) {
          controller.rotateLeft
        }
        if (dir.equals("Right")) {
          controller.rotateRight
        }
        Ok(views.html.Application._freshCard(controller.getPlayfield, manicanList())).as(HTML)
      }
      .getOrElse {
        println(request.body)
        Ok("error")
      }
  }

  def rules() = Action {
    Ok(views.html.rules())
  }

}
