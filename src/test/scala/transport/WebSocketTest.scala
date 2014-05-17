package transport

import io.backchat.hookup._
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
    val client = clientFor("ws://localhost:8080/ws/game/123")
  }

  "test" in new ctx {
    import client._
    
    connect()

    send(TextMessage(Click(24).toJSONString))

    messages must contain[InboundMessage](JsonMessage(InvalidMove().toJSONAst)).eventually

    skipped
  }
}
