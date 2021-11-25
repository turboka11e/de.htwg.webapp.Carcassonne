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

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be
    * called when the application receives a `GET` request with a path of `/`.
    */
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def rules(): Action[AnyContent] = Action {
    Ok(views.html.rules())
  }

}
