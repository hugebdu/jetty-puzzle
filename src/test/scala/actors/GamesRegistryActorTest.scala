package actors

import akka.testkit.{TestProbe, TestActorRef}
import akka.pattern.ask
import actors.GamesRegistryActor._
import akka.util.Timeout
import concurrent.duration._
import model.{Size, Board, Turn, Id}
import scala.concurrent.{Await, Future}
import akka.actor.{Actor, ActorRef}
import actors.GamesRegistryActor.CreateInvitation
import actors.GamesRegistryActor.Join
import actors.GameActor.Init



class GamesRegistryActorTest extends ActorSpec {
  
  trait ctx extends ActorScope {

    implicit val size = Size(4)

    val imageUrl = "some-image.gif"

    implicit val timeout = Timeout(1.second)

    val board = Board.create()

    trait TestProbeGameActorConstruction extends GameActorConstruction { this: Actor =>
      import collection.mutable
      val probes = mutable.ListBuffer.empty[(String, TestProbe)]
      override def createGameActor(id: String): ActorRef = {
        val probe = TestProbe()
        probes += (id -> probe)
        probe.ref
      }
    }

    trait StubbedBoardConstruction extends BoardConstruction {
      override def createBoard(): Board = {
        board
      }
    }

    val games = TestActorRef(new GamesRegistryActor with TestProbeGameActorConstruction with StubbedBoardConstruction)

    val player1 = TestProbe()
    val player2 = TestProbe()

    def await[T](f: Future[T]): T = {
      Await.result(f, timeout.duration)
    }
    
    def idForNewInvitation() = {
      await {
        (games ? CreateInvitation(imageUrl)).mapTo[String]
      }
    }
  }

  "CreateInvitation" should {

    "return new id" in new ctx {
      games ? CreateInvitation(imageUrl) must beAnInstanceOf[String].await
    }
  }

  "Join" should {

    "issue UnknownInvitation when no invitation found" in new ctx {

      games ! Join(Id.random(), player1.ref)

      player1.expectMsg(UnknownInvitation)
    }

    "issue InitGame upon both players joined, constructing and initializing GameActor" in new ctx {
      val id = idForNewInvitation()

      player1.ignoreMsg {
        case WaitingForPair => true
      }

      games ! Join(id, player1.ref)
      games ! Join(id, player2.ref)

      games.underlyingActor.probes must haveSize(1)

      val (gameId, gameProbe) = games.underlyingActor.probes(0)

      gameId must be_===(id)

      player1.expectMsg(InitGame(id, Turn.Left, imageUrl, gameProbe.ref))
      player2.expectMsg(InitGame(id, Turn.Right, imageUrl, gameProbe.ref))
      gameProbe.expectMsg(Init(Player(player1.ref, board), Player(player2.ref, board)))
    }

    "remove Invitation for initialized game" in new ctx {
      val id = idForNewInvitation()

      player1.ignoreMsg {
        case WaitingForPair => true
        case i: InitGame => true
      }

      games ! Join(id, player1.ref)
      games ! Join(id, player2.ref)

      games ! Join(id, player1.ref)

      player1.expectMsg(UnknownInvitation)
    }

    "issue WaitingForPair when invitation exists" in new ctx {
      val id =  await {
        (games ? CreateInvitation("some-image.gif")).mapTo[String]
      }

      games ! Join(id, player1.ref)
      games ! Join(id, player1.ref)

      player1.expectMsg(WaitingForPair)
      player1.expectMsg(WaitingForPair)
    }
  }
}



