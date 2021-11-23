package utils

import de.htwg.se.Carcassonne.controller.controllerComponent.ControllerInterface

object Utils {

  var (last_row, last_col) = (0, 0)

  def manicanList(controller: ControllerInterface): List[(String, String)] = {
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

}
