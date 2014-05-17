package drivers

import actors.GamesRegistryActor.CreateInvitation
import akka.pattern.ask
import scala.concurrent.Await
import concurrent.duration._
import org.specs2.time.NoTimeConversions
import akka.util.Timeout

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
trait GamesRegistryDriver extends NoTimeConversions { this: ServerSpec =>

  implicit val timeout = Timeout(1.second)

  def givenGameInvitation(imageUrl: String = "image.gif"): String = {
    Await.result((server.gamesRegistry ? CreateInvitation(imageUrl)).mapTo[String], 1.second)
  }
}
