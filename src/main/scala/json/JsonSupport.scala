package json

import org.json4s.native.Serialization
import actors.Messages.Message
import org.json4s._
import actors.Messages
import scala.util.Try

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
trait JsonSupport {

  implicit val formats = MessagesFormats

  def asString(msg: Message): String = {
    Serialization.write(msg)
  }
  
  def asJson(msg: Message): JValue = {
    Extraction.decompose(msg)
  }

  def asMessage(s: String): Option[Message] = {
    Try {
      Serialization.read[Message](s)
    }.toOption
  }
}

object JsonSupport extends JsonSupport

private[json] object MessagesFormats extends DefaultFormats {
  override val typeHints: TypeHints = MessagesTypeHints
  override val typeHintFieldName: String = "type"
}

private[json] object MessagesTypeHints extends TypeHints {
  def hintFor(clazz: Class[_]) = clazz.getName.substring(clazz.getName.lastIndexOf("$")+1)
  def classFor(hint: String) = hints find (hintFor(_) == hint)
  val hints = Messages.MessageTypes
}
