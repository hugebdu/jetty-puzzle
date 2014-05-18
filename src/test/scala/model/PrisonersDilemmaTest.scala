package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class PrisonersDilemmaTest extends SpecificationWithJUnit with Mockito {

  trait ctx extends Scope {
    val leftBoard = mock[Board]
    val rightBoard = mock[Board]

    val dilemma = PrisonersDilemma()
  }
}
