package model

import org.specs2.mutable.SpecificationWithJUnit

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class DistanceMapTest extends SpecificationWithJUnit {

  "apply" should {

    "generate valid distance map - simple case" >> {

      implicit val size = Size(2)

      val map = DistanceMap()

      map.distances(Position(0)) must contain(exactly(
        Position(3),
        Position(1),
        Position(2),
        Position(0)
      )).inOrder
    }

    "generate valid distance map - other case" >> {

      implicit val size = Size(3)

      val map = DistanceMap()

      map.distances(Position(4)) must contain(exactly(
        Position(0),
        Position(2),
        Position(6),
        Position(8),
        Position(1),
        Position(3),
        Position(5),
        Position(7),
        Position(4)
      )).inOrder
    }
  }
}
