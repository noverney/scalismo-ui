package scalismo.ui.view.menu

import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.action.ShowDisplayScalingDialogAction
import scalismo.ui.view.perspective.PerspectiveFactory
import scalismo.ui.view.{ PerspectivePanel, ScalismoFrame }

import scala.swing.event.{ ButtonClicked, Key }
import scala.swing.{ Menu, MenuItem, RadioMenuItem }

class ViewMenu extends Menu("View") {
  mnemonic = Key.V
}

object ViewMenu {

  class ShowDisplayScalingDialogItem(implicit val frame: ScalismoFrame) extends MenuItem(new ShowDisplayScalingDialogAction) {
    mnemonic = Key.D
  }

  class PerspectiveMenu(implicit val frame: ScalismoFrame) extends Menu("Perspective") {
    mnemonic = Key.P
    icon = BundledIcon.Perspective.standardSized()

    val panel = frame.perspective

    private class PerspectiveMenuItem(val factory: PerspectiveFactory) extends RadioMenuItem(factory.perspectiveName) with ScalismoPublisher {

      def updateUi() = {
        selected = panel.perspective == factory
      }

      listenTo(panel)

      reactions += {
        case ButtonClicked(_) => panel.perspective = factory
        case PerspectivePanel.event.PerspectiveChanged(_, _, _) => updateUi()
      }

      updateUi()
    }

    PerspectiveFactory.factories.foreach { pf =>
      contents += new PerspectiveMenuItem(pf)
    }

  }

}

