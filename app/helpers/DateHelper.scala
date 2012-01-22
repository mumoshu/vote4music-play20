package helpers

import scala.util.control.Exception.catching

import java.util.Date
import java.text.SimpleDateFormat

object DateHelper {

  case class RichDate(date: Date) {
    def format(str: String): String = {
      new SimpleDateFormat(str).format(date)
    }
  }
  
  implicit def richDate(date: Date): RichDate = RichDate(date)
  
}
