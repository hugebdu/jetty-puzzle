package actors

import akka.actor.{Props, ActorRef, Actor}
import actors.GamesRegistryActor._
import model._
import collection.mutable
import model.Size
import actors.GamesRegistryActor.CreateInvitation
import actors.GamesRegistryActor.Join
import actors.GameActor.Init

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
class GamesRegistryActor extends Actor with GameActorConstruction with BoardConstruction {

  val invitations = mutable.Map.empty[String, Invitation]

  def receive: Receive = {

    case CreateInvitation(image) =>

      val invitation = Invitation(image = image)
      val id = Id.random()
      invitations += (id -> invitation)
      sender ! id

    case Join(id, player) =>

      invitations.get(id).fold[Unit](player ! UnknownInvitation) {

        case i @ Invitation(_, Nil) =>
          i.team = player :: i.team
          player ! WaitingForPair

        case Invitation(_, `player` :: Nil) =>
          player ! WaitingForPair

        case Invitation(imageUrl, left :: Nil) =>
          val game = createGameActor(id)
          val right = player
          left ! InitGame(id, Turn.Left, imageUrl, game)
          right ! InitGame(id, Turn.Right, imageUrl, game)
          game ! Init(Player(left, createBoard()), Player(right, createBoard()))
          invitations -= id
      }
  }

  case class Invitation(image: String, var team: List[ActorRef] = Nil)
}

trait BoardConstruction {

  implicit val size = Size(4)

  def createBoard(): Board = {
    val b = Board.create()
    b.shuffle()
    b
  }
}

trait GameActorConstruction { this: Actor =>

  def createGameActor(id: String): ActorRef = {
    context.actorOf(Props(new GameActor(DefaultSurpriseProducer)), s"Game-$id")
  }
}

object GamesRegistryActor {
  case class InitGame(id: String, turn: Turn, imageUrl: String, game: ActorRef)
  case class CreateInvitation(imageUrl: String)
  case class Join(id: String, player: ActorRef)
  case object UnknownInvitation
  case object WaitingForPair
}
