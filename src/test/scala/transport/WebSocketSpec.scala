package transport

import org.specs2.mutable.Specification
import io.backchat.hookup._
import java.net.URI
import org.specs2.specification.Scope
import drivers.WebSocketDriver
import io.backchat.hookup.TextMessage

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
class WebSocketSpec extends Specification {

  val server = new PuzzleServer

  step {
    server.start()
    while (!server.isStarted)
      Thread.sleep(50)
  }

  trait ctx extends Scope with WebSocketDriver {
    val uri = new URI("ws://localhost:8080/ws/hello")
  }

  "test" in new ctx {
    connect()

    send(TextMessage("hello"))

    messages must contain[InboundMessage](TextMessage("world")).eventually
  }

  step {
    server.stop()
  }
}
