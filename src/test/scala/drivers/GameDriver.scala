package drivers

import actors.GamesRegistryActor.CreateInvitation
import akka.pattern.ask
import scala.concurrent.Await
import concurrent.duration._
import org.specs2.time.NoTimeConversions
import akka.util.Timeout
import actors.Messages.Message

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
trait GameDriver extends NoTimeConversions with WebSocketDriver { this: ServerSpec =>

  implicit val timeout = Timeout(1.second)

  def givenInitializedGame(pf: PartialFunction[Message, Message] = Map.empty) = {
    val id = givenGameInvitation()
    val p1 = playerFor(id)
    val p2 = playerFor(id)

    p1.respond(pf)
    p2.respond(pf)

    Seq(p1, p2) foreach { _.connect() }
    (p1, p2)
  }

  def givenGameInvitation(imageUrl: String = "image.gif"): String = {
    Await.result((server.gamesRegistry ? CreateInvitation(imageUrl)).mapTo[String], 1.second)
  }
}
