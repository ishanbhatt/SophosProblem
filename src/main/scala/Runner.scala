import sys.process._
import java.net.URL
import java.io.File
import java.text.SimpleDateFormat
import java.util.TimeZone

import scala.util.matching.Regex
import net.lingala.zip4j.core.ZipFile

import scala.io.Source

object Runner {

  val base_url = "http://mbd.hu/uris/"
  var all_urls: Array[String] = Array()

  def extract_url_append_list(zipPath: String) = {
    val zipFile: ZipFile = new ZipFile(zipPath)
    zipFile.setPassword(zipPath)
    zipFile.extractAll(new File(".").getCanonicalPath)
    val lines = Source.fromFile("urilist.txt").getLines().toArray
    all_urls = all_urls ++ lines
    new File("urilist.txt").delete()
  }

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

    for (i <- 1 until(200)){
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
        println(s"Epoch hasn't changed not doing another download for iteration $i")
        Thread.sleep(2000)
      }
    }
    passwordZipped map extract_url_append_list

    val mostCommonUri = all_urls.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
//    scala> val s = Seq("apple", "oranges", "apple", "banana", "apple", "oranges", "oranges")
//    s: Seq[String] = List(apple, oranges, apple, banana, apple, oranges, oranges)
//
//    scala> s.groupBy(identity)
//    res0: scala.collection.immutable.Map[String,Seq[String]] = Map(banana -> List(banana), oranges -> List(oranges, oranges, oranges), apple -> List(apple, apple, apple))
//
      // We can groupBy anything say _.head(First Char), _.length(Size of the word) , _.sort(Anagram calculation maybe)
//    scala> s.groupBy(identity).mapValues(_.size)
//    res1: scala.collection.immutable.Map[String,Int] = Map(banana -> 1, oranges -> 3, apple -> 3)
//
//    scala> s.groupBy(identity).mapValues(_.size).maxBy(_._2)
//    res2: (String, Int) = (oranges,3)
//
//    scala> s.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
//    res3: String = oranges

    println(scala.io.Source.fromURL(mostCommonUri).mkString)
  }
}
