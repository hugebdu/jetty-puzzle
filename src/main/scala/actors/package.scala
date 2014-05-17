import actors.Messages.Message
import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.json4s._

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
package object actors {

  import json.JsonSupport._

  implicit class RichRemoteEndpoint(val endpoint: RemoteEndpoint) extends AnyVal {
    def ! (msg: Message): Unit = {
      endpoint.sendString(msg.toJSONString)
    }
  }

  implicit class MessageWithJsonString(val msg: Message) extends AnyVal {
    def toJSONString: String = asString(msg)
    def toJSONAst: JValue = asJson(msg)
  }

  implicit class StringWithAsMessage(val s: String) extends AnyVal {
    def toMessage: Option[Message] = asMessage(s)
  }
}
