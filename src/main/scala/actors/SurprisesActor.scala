package actors

import akka.actor.Actor
import concurrent.duration._
import scala.util.Random

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/20/14
 */
class SurprisesActor extends Actor {

  import context._

  override def preStart(): Unit = {
    schedule()
  }

  def receive: Actor.Receive = {
    case SurprisesActor.Boom =>
      context.actorSelection("../games-registry/*") ! GameActor.CheckForSurprises
      schedule()
  }

  private def schedule(): Unit = {
    system.scheduler.scheduleOnce(randomDuration, self, SurprisesActor.Boom)
  }

  private def randomDuration = {
    (15 + Random.nextInt(15)).seconds
  }
}

object SurprisesActor {
  case object Boom
}
