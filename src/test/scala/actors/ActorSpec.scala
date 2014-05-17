package actors

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.time.NoTimeConversions
import org.specs2.mock.Mockito
import akka.actor.ActorSystem
import akka.testkit.{TestKit, ImplicitSender, TestKitBase}
import org.specs2.specification.{After, Scope}

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
abstract class ActorSpec extends SpecificationWithJUnit with NoTimeConversions with Mockito {

  trait ActorScope extends TestKitBase with ImplicitSender with Scope with After {

    implicit lazy val system = ActorSystem()

    def after = {
      TestKit.shutdownActorSystem(system)
    }
  }
}
