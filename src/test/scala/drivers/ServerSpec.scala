package drivers

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.{Step, Fragments}
import transport.PuzzleServer
import org.specs2.time.NoTimeConversions



trait ServerSpec extends SpecificationWithJUnit with NoTimeConversions {

  val server = new PuzzleServer

  sequential

  override def map(fs: => Fragments): Fragments = Step(startServer()) ^ fs ^ Step(stopServer())

  def startServer(): Unit = {
    server.start()
    while (!server.isStarted)
      Thread.sleep(50)
  }

  def stopServer(): Unit = {
    server.stop()
  }
}
