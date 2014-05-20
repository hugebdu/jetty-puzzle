package actors

import akka.testkit.{TestProbe, TestActorRef}
import actors.GamesRegistryActor.{UnknownInvitation, WaitingForPair}
import concurrent.duration._
import model.{Turn, Id}
import actors.GameActor._
import akka.actor.ActorRef
import actors.Messages.Message
import actors.GameActor.PickedSurprise
import model.Surprise.Challenge
import actors.GameActor.Swap
import actors.GameActor.GameFinished
import actors.GameActor.Click



class PlayerActorTest extends ActorSpec {

  implicit class ActorRefWithJsonSend(ref: ActorRef) {

    def !! (m: Message): Unit = {
      ref ! m.toJSONString
    }
  }

  trait ctx extends ActorScope {

    def afterSome(d: Duration) = MockitoVerificationWithTimeout(d)

    val endpoint = mock[Endpoint]
    val game = TestProbe()

    val player = TestActorRef(new PlayerActor(endpoint))
  }

  trait gameInitialized extends ctx {
    player ! GamesRegistryActor.InitGame(Id.random(), Turn.Left, "image.gif", game.ref)
  }

  trait challenged extends ctx {
    player ! Challenge("prisoner")
  }

  "StartGame" should {

    "propagate to endpoint" in new ctx with gameInitialized {
      player ! StartGame(Seq(1 -> 2))

      got {
        one(endpoint) ! Messages.StartGame(Seq(1 -> 2))
      }
    }
  }

  "InvalidMove" should {

    "propagate to endpoint" in new ctx with gameInitialized {
      player ! InvalidMove

      got {
        one(endpoint) ! Messages.InvalidMove()
      }
    }
  }

  "CompleteSurprise" should {

    "propagate to endpoint" in new ctx with gameInitialized with challenged {
      player ! CompleteSurprise(Seq(1 -> 2, 3 -> 4))

      got {
        one(endpoint) ! Messages.ChallengeFinished(Seq(1 -> 2, 3 -> 4))
      }
    }
  }

  "ChallengeOutcome" should {

    "propagate picked to game" in new ctx with gameInitialized with challenged {
      player !! Messages.ChallengeOutcome(picked = true)

      game.expectMsg(PickedSurprise(Turn.Left))
    }

    "propagate dropped to game" in new ctx with gameInitialized with challenged {
      player !! Messages.ChallengeOutcome(picked = false)

      game.expectMsg(DroppedSurprise(Turn.Left))
    }
  }
  
  "Challenge" should {

    "propagate to endpoint" in new ctx with gameInitialized {
      
      player ! Challenge("prisoner")
      
      got {
        one(endpoint) ! Messages.Challenge("prisoner", PlayerActor.ChallengeTimeoutInSeconds)
      }
    }

    "stop accepting Click messages" in new ctx with gameInitialized {

      player ! Challenge("prisoner")

      player !! Messages.Click(24)

      game.expectNoMsg(3.seconds)
    }
  }

  "Swap" should {

    "propagate to client" in new ctx with gameInitialized {
      player ! Swap(Seq(1 -> 2))

      got {
        one(endpoint) ! Messages.Swap(Seq(1 -> 2))
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
      player !! Messages.Click(24)

      game.expectNoMsg()
    }

    "propagated to game actor" in new ctx with gameInitialized {
      player !! Messages.Click(24)

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
