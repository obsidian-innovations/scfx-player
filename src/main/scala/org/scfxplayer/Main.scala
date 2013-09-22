package org.scfxplayer

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.effect._
import scalafx.scene.paint.{ Stops, LinearGradient }
import scalafx.scene.text.Text
import javafx.event.EventHandler
import javafx.stage.WindowEvent

// *************** Some reading material **************** //
//http://search.maven.org/#search%7Cga%7C1%7Cscalafx
//http://raintomorrow.cc/post/50811498259/how-to-package-a-scala-project-into-an-executable-jar
//http://codingonthestaircase.wordpress.com/2013/09/11/what-is-new-in-scalafx-8-milestone-1/
//https://github.com/jugchennai/scalafx.g8/blob/master/src/main/g8/build.sbt
//http://java.dzone.com/articles/getting-started-scalafx-and

object Main extends JFXApp {

  val scalaText = new Text {
    text = "Scala"
    style = "-fx-font-size: 100pt"
    fill = new LinearGradient(
      endX = 0,
      stops = Stops(PALEGREEN, SEAGREEN))
  }

  val fxText = new Text {
    text = "FX"
    style = "-fx-font-size: 100pt"
    fill = new LinearGradient(
      endX = 0,
      stops = Stops(CYAN, DODGERBLUE))
    effect = new DropShadow {
      color = DODGERBLUE
      radius = 25
      spread = 0.25
    }
  }

  val playerText = new Text {
    text = " Player"
    style = "-fx-font-size: 100pt"
    fill = new LinearGradient(
      endX = 0,
      stops = Stops(PALEGREEN, SEAGREEN))
  }

  val textSeq = Seq(scalaText, fxText, playerText)

  val textBox = new HBox {
    content = textSeq
    effect = new Reflection {
      topOffset = -50
      bottomOpacity = 0.75
      input = new Lighting {
        light = new Light.Distant {
          elevation = 60
        }
      }
    }
  }

  stage = new PrimaryStage {
    title = "ScalaFX Demo Player"
    width = 650
    height = 450
    scene = new Scene {
      fill = BLACK
      content = textBox
    }
    onShown = new EventHandler[WindowEvent] {
      override def handle(event:WindowEvent) {
        event.consume()
        stage.setWidth(textSeq.foldLeft(0.0){(r, c) => c.boundsInLocal.value.getWidth + r})
        stage.setHeight(textBox.boundsInLocal.value.getHeight)
      }
    }
  }

}
