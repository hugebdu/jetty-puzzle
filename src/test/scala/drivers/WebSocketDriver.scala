package drivers

import java.net.URI
import io.backchat.hookup._
import io.backchat.hookup.HookupClient._
import io.backchat.hookup.HookupClientConfig
import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.matcher.{Matchers, Matcher}
import actors.Messages.Message
import scala.collection.GenTraversable

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
trait WebSocketDriver {

  import collection.mutable

  def clientFor(uri: String) = new {

    private[this] val messagesBuffer = new mutable.ArrayBuffer[InboundMessage] with mutable.SynchronizedBuffer[InboundMessage]

    private[this] lazy val client = new DefaultHookupClient(HookupClientConfig(new URI(uri))) {
      def receive: Receive = {
        case m: InboundMessage => messagesBuffer += m
      }
    }

    def connect(): Unit = {
      Await.result(client.connect(), 3.seconds)
    }

    def send(msg: OutboundMessage): Unit = {
      client.send(msg)
    }

    def messages: Seq[InboundMessage] = {
      messagesBuffer
    }
  }

  def containMessage(msg: Message): Matcher[GenTraversable[InboundMessage]] = {
    import Matchers._
    contain[InboundMessage](JsonMessage(msg.toJSONAst)).eventually
  }
}
