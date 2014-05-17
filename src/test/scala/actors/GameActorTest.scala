package actors

import akka.testkit._
import model._
import collection.mutable
import model.Size
import model.Piece
import GameActor._

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/16/14
 */
class GameActorTest extends ActorSpec {

  trait ctx extends ActorScope {

    implicit val size = Size(4)

    val leftPlayer = TestProbe()
    val rightPlayer = TestProbe()
    val termination = TestProbe()

    val leftBoard = mock[Board]
    val rightBoard = mock[Board]
    
    val surprises = mock[SurpriseProducer]

    val game = TestActorRef(new GameActor(surprises))

    def givenGameStarted = {
      game ! Init(Player(leftPlayer.ref, leftBoard), Player(rightPlayer.ref, rightBoard))
      Unit
    }
  }

  trait startedGame extends ctx {
    givenGameStarted
    leftPlayer.expectMsgType[StartGame]
    rightPlayer.expectMsgType[StartGame]
  }

  "Init" should {

    "initialize state and send StartGame to pairs" in new ctx {
      leftBoard.cells returns mutable.IndexedSeq(Piece(1))
      rightBoard.cells returns mutable.IndexedSeq(Piece(2))

      game ! Init(Player(leftPlayer.ref, leftBoard), Player(rightPlayer.ref, rightBoard))

      leftPlayer.expectMsg(StartGame(Turn.Left, Seq(Piece(1))))
      rightPlayer.expectMsg(StartGame(Turn.Right, Seq(Piece(2))))
    }
  }
  
  "CheckForSurprises" should {

    "on surprise - send ask to pairs" in new startedGame {
      surprises.maybeSurprise(leftBoard -> rightBoard) returns Some(DummySurprise)
      game ! CheckForSurprises

      leftPlayer.expectMsg(DummySurprise.DummyAsk)
      rightPlayer.expectMsg(DummySurprise.DummyAsk)
    }

    "on surprise - ignore clicks" in new startedGame {
      surprises.maybeSurprise(leftBoard -> rightBoard) returns Some(DummySurprise)
      leftBoard.click(14) returns None

      game ! CheckForSurprises

      game ! Click(Turn.Left, 14)

      expectNoMsg()
    }

    "do nothing when no surprise" in new startedGame {
      surprises.maybeSurprise(leftBoard -> rightBoard) returns None
      game ! CheckForSurprises
      
      leftPlayer.expectNoMsg()
      rightPlayer.expectNoMsg()
    }
  }

  "Click" should {

    "send InvalidMove if the move is illegal" in new startedGame {
      leftBoard.click(14) returns None

      game ! Click(Turn.Left, 14)

      expectMsg(InvalidMove)
    }

    "send Swap to the sender if move is valid" in new startedGame {
      leftBoard.click(14) returns Some(14 -> 13)

      game ! Click(Turn.Left, 14)

      expectMsg(Swap(14 -> 13))
    }

    "send GameOver to both upon finish and terminate" in new startedGame {
      termination watch game

      leftBoard.click(14) returns Some(14 -> 13)
      leftBoard.isCompleted returns true

      game ! Click(Turn.Left, 14)

      leftPlayer.expectMsg(GameFinished(Turn.Left))
      rightPlayer.expectMsg(GameFinished(Turn.Left))

      termination.expectTerminated(game)
    }
  }
}

case object DummySurprise extends Surprise {

  val question = DummyAsk

  type Ask = DummyAsk.type

  case object DummyAsk
}