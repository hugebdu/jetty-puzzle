package drivers

import java.net.URI
import io.backchat.hookup._
import io.backchat.hookup.HookupClient._
import io.backchat.hookup.HookupClientConfig
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
trait WebSocketDriver {

  import collection.mutable

  def uri: URI

  private[this] val messagesBuffer = new mutable.ArrayBuffer[InboundMessage] with mutable.SynchronizedBuffer[InboundMessage]

  private[this] lazy val client = new DefaultHookupClient(HookupClientConfig(uri)) {
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
