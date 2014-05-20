package model

import java.util.UUID



object Id {

  def random(): String = {
    UUID.randomUUID().toString
  }
}
