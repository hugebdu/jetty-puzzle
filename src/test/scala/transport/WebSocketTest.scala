package transport

import io.backchat.hookup._
import java.net.URI
import org.specs2.specification.Scope
import drivers.{ServerSpec, WebSocketDriver}
import actors.Messages.{InvalidMove, Click}
import json.JsonSupport

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
class WebSocketTest extends ServerSpec {

  trait ctx extends Scope with WebSocketDriver with JsonSupport {
    val uri = new URI("ws://localhost:8080/ws/hello")
  }

  "test" in new ctx {
    connect()

    send(TextMessage(Click(24).toJSONString))

    messages must contain[InboundMessage](JsonMessage(InvalidMove().toJSONAst)).eventually
  }
}
