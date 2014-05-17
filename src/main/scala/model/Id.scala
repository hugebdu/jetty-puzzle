package model

import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
object Id {

  def random(): String = {
    UUID.randomUUID().toString
  }
}
