import sys.process._
import java.net.URL
import java.io.File
import java.text.SimpleDateFormat
import java.util.TimeZone

import scala.util.matching.Regex

object Runner {

  val base_url = "http://mbd.hu/uris/"
  case class StringTimeStamp(str: String) {

    val pattern = new Regex("[0-9]{4}_[0-9]{2}_[0-9]{2}_[0-9]{2}_[0-9]{2}_[0-9]{2}")

    def getDatePattern = {
      pattern.findFirstIn(str).get
    }

    def getEpoch = {
      val timestamp: SimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
      timestamp.setTimeZone(TimeZone.getTimeZone("GMT"))
      val date = timestamp.parse(getDatePattern)
      (date.getTime/1000).toString
    }

    def makeUrl = {
      base_url + getDatePattern + "/urilist.zip"
    }
  }

  implicit def strToStrTS(str: String) = StringTimeStamp(str)


  def main(args: Array[String]): Unit = {

    var passwordZipped : Array[String] = Array()
    var text: String = scala.io.Source.fromURL(base_url).mkString
    var oldEpoch = text.getEpoch
    var newEpoch = ""
    var url = ""

    for (_ <- 1 until(50)){
      text = scala.io.Source.fromURL(base_url).mkString
      newEpoch = text.getEpoch

      if (oldEpoch != newEpoch){
        val thread = new Thread {
          url = text.makeUrl
          new URL(url) #> new File(oldEpoch) !!

          passwordZipped = passwordZipped :+ oldEpoch
          oldEpoch = newEpoch
        }
        thread.start
      }
      else {
        println("Epoch hasn't changed not doing another download")
        Thread.sleep(2000)
      }

    }

  }
}
