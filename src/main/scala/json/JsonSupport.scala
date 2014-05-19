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

  def asMessage(json: JValue): Option[Message] = {
    json.extractOpt[Message]
  }

  def asMessage(s: String): Option[Message] = {
    Try { Serialization.read[Message](s) }.toOption
  }
}

object JsonSupport extends JsonSupport

private[json] object PairOfIndexesSerializer extends CustomSerializer[(Int, Int)](format => ({
    case JObject(List(JField(leftAsString, JString(rightAsString)))) => (leftAsString.toInt, rightAsString.toInt)
  },
  {
    case (left: Int, right: Int) => JObject(JField(left.toString, JString(right.toString)))
  })
)

private[json] object MessagesFormats extends DefaultFormats {
  override val typeHints: TypeHints = MessagesTypeHints
  override val typeHintFieldName: String = "type"
  override val customSerializers: List[Serializer[_]] = PairOfIndexesSerializer :: Nil
}

private[json] object MessagesTypeHints extends TypeHints {
  def hintFor(clazz: Class[_]) = clazz.getName.substring(clazz.getName.lastIndexOf("$")+1)
  def classFor(hint: String) = hints find (hintFor(_) == hint)
  val hints = Messages.MessageTypes
}
