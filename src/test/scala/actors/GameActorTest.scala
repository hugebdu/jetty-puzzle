package actors

import akka.testkit._
import model._
import model.Size
import GameActor._
import model.Surprise.Challenge

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
    val surprise = mock[Surprise]

    surprise.challenge() returns Challenge("prisoner")

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

  trait surprised extends ctx {
    surprises.maybeSurprise(leftBoard -> rightBoard) returns Some(surprise)
    game ! CheckForSurprises
    leftPlayer.expectMsgType[Challenge]
    rightPlayer.expectMsgType[Challenge]
  }

  "PickedSurprise/DroppedSurprise" should {

    "complete the surprise on both answers" in new ctx with startedGame with surprised {

      surprise.handle(leftBoard -> Surprise.Drop, rightBoard -> Surprise.Pick) returns ((CompleteSurprise(Seq(1 -> 2)), CompleteSurprise(Seq(3 -> 4))))

      game ! DroppedSurprise(Turn.Left)
      game ! PickedSurprise(Turn.Right)

      leftPlayer.expectMsg(CompleteSurprise(Seq(1 -> 2)))
      rightPlayer.expectMsg(CompleteSurprise(Seq(3 -> 4)))
    }
  }

  "Init" should {

    "initialize state and send StartGame to pairs" in new ctx {
      leftBoard.shuffles returns Seq(0 -> 1)
      rightBoard.shuffles returns Seq(2 -> 3)

      game ! Init(Player(leftPlayer.ref, leftBoard), Player(rightPlayer.ref, rightBoard))

      leftPlayer.expectMsg(StartGame(Seq(0 -> 1)))
      rightPlayer.expectMsg(StartGame(Seq(2 -> 3)))
    }
  }
  
  "CheckForSurprises" should {

    "on surprise - send ask to pairs" in new startedGame {
      surprises.maybeSurprise(leftBoard -> rightBoard) returns Some(surprise)
      game ! CheckForSurprises

      leftPlayer.expectMsg(Challenge("prisoner"))
      rightPlayer.expectMsg(Challenge("prisoner"))
    }

    "on surprise - ignore clicks" in new startedGame {
      surprises.maybeSurprise(leftBoard -> rightBoard) returns Some(surprise)
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

      expectMsg(Swap(Seq(14 -> 13)))
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
