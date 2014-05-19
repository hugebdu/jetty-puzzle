package drivers

import java.net.URI
import io.backchat.hookup._
import io.backchat.hookup.HookupClient._
import io.backchat.hookup.HookupClientConfig
import scala.concurrent.Await
import org.specs2.matcher.{Matchers, Matcher}
import actors.Messages.Message
import scala.collection.GenTraversable
import json.JsonSupport._
import org.specs2.time.NoTimeConversions
import concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
trait WebSocketDriver extends Matchers with NoTimeConversions {

  import collection.mutable

  def playerFor(gameId: String): Client = clientFor(s"ws://localhost:8080/ws/game/$gameId")

  def clientFor(uri: String): Client = new Client(uri)

  def isMessage(msg: Matcher[Message]): Matcher[InboundMessage] = {
    beAnInstanceOf[JsonMessage] and
    beSome[Message](msg) ^^ { (m: InboundMessage) => asMessage(m.asInstanceOf[JsonMessage].content) }
  }

  def containMessage(msg: Matcher[Message]): Matcher[GenTraversable[InboundMessage]] = {
    contain[InboundMessage](isMessage(msg))
  }

  def containMessage(msg: Message): Matcher[GenTraversable[InboundMessage]] = {
    containMessage(be_===(msg))
  }

  def containMessageEventually(msg: Message): Matcher[GenTraversable[InboundMessage]] = {
    containMessage(msg).eventually(retries = 20, sleep = 200.milliseconds)
  }

  def containMessageEventually(msg: Matcher[Message]): Matcher[GenTraversable[InboundMessage]] = {
    containMessage(msg).eventually(retries = 20, sleep = 200.milliseconds)
  }

  class Client(uri: String) {

    private[this] val messagesBuffer = new mutable.ArrayBuffer[InboundMessage] with mutable.SynchronizedBuffer[InboundMessage]

    private[this] var responses: PartialFunction[Message, Message] = Map.empty

    private[this] lazy val client = new DefaultHookupClient(HookupClientConfig(new URI(uri))) {

      def receive: Receive = {
        case m: InboundMessage =>
          println { m }
          messagesBuffer += m
          m match {
            case JsonMessage(json) =>
              for (msg <- asMessage(json)) if (responses.isDefinedAt(msg)) Client.this.send(responses(msg))
            case _ => ()
          }
      }
    }

    def connect(): Unit = {
      Await.result(client.connect(), 3.seconds)
    }

    def send(msg: OutboundMessage): Unit = {
      client.send(msg)
    }

    def send(m: Message): Unit = {
      client.send(JsonMessage(m.toJSONAst))
    }

    def respond(pf: PartialFunction[Message, Message]): Unit = {
      responses = pf
    }

    def messages: Seq[InboundMessage] = {
      messagesBuffer
    }
  }
}
