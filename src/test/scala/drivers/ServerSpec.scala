package drivers

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.{Step, Fragments}
import transport.PuzzleServer

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
trait ServerSpec extends SpecificationWithJUnit {

  val server = new PuzzleServer

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
