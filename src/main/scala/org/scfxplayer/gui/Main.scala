package org.scfxplayer.gui

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.stage.{Stage, WindowEvent}
import scalafx.scene.image.Image

object Main extends JFXApp {
  import scalafx.Includes._

  def getStage():Stage = stage
  val playerView = new PlayerView(getStage)

  stage = new PrimaryStage {
    title = "Demo ScalaFX Player"
    icons ++= Seq(new Image(getClass.getResource("/app-icon-32.png").toExternalForm))
    minHeight = 300
    minWidth = 400
    width = 640
    height = 480
    scene = new Scene {
      stylesheets ++= List("default-skin.css")
      width onChange {
        (s, oldval, newval) => {
          playerView.setPrefWidth(scene.value.getWidth)
          playerView.onWidthUpdated(oldval.doubleValue, newval.doubleValue)
        }
      }
      height onChange {playerView.setPrefHeight(scene.value.getHeight);}
      content = playerView
      onCloseRequest = (event:WindowEvent) => {
        playerView.playerControls.stop()
      }
      onShowing = (event:WindowEvent) => { playerView.plMgr.loadDefaultPlaylist() }
    }
  }

}
