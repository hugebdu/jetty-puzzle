package actors

import akka.testkit.{TestProbe, TestActorRef}
import actors.GamesRegistryActor.{UnknownInvitation, WaitingForPair}
import concurrent.duration._
import model.{Turn, Id}
import actors.GameActor.{Swap, GameFinished, Click}

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
class PlayerActorTest extends ActorSpec {

  trait ctx extends ActorScope {

    def afterSome(d: Duration) = MockitoVerificationWithTimeout(d)

    val endpoint = mock[Endpoint]
    val game = TestProbe()

    val player = TestActorRef(new PlayerActor(endpoint))
  }

  trait gameInitialized extends ctx {
    player ! GamesRegistryActor.InitGame(Id.random(), Turn.Left, "image.gif", game.ref)
  }

  "Swap" should {

    "propagate to client" in new ctx with gameInitialized {
      player ! Swap(1 -> 2)

      got {
        one(endpoint) ! Messages.Swap(1 -> 2)
      }
    }
  }

  "InitGame" should {

    "propagate to endpoint" in new ctx {
      player ! GamesRegistryActor.InitGame(Id.random(), Turn.Left, "image.gif", game.ref)

      got {
        one(endpoint) ! Messages.InitGame("image.gif")
      }
    }
  }

  "UnknownInvitation" should {

    "propagate to endpoint" in new ctx {
      player ! UnknownInvitation

      got {
        one(endpoint) ! Messages.UnknownInvitation()
      }
    }
  }

  "Click" should {

    "be ignored when not in a game" in new ctx {
      player ! Messages.Click(24)

      game.expectNoMsg()
    }

    "propagated to game actor" in new ctx with gameInitialized {
      player ! Messages.Click(24).toJSONString

      game.expectMsg(Click(Turn.Left, 24))
    }
  }

  "GameFinished" should {

    "propagate to endpoint - winner" in new ctx with gameInitialized {
      player ! GameFinished(Turn.Left)

      got {
        afterSome(1.second).one(endpoint) ! Messages.GameFinished(winner = true)
      }
    }

    "propagate to endpoint - looser" in new ctx with gameInitialized {
      player ! GameFinished(Turn.Right)

      got {
        one(endpoint) ! Messages.GameFinished(winner = false)
      }
    }
  }

  "WaitingForPair" should {

    "propagate to endpoint" in new ctx {
      player ! WaitingForPair

      got {
        one(endpoint) ! Messages.WaitingForPair()
      }
    }
  }
}
