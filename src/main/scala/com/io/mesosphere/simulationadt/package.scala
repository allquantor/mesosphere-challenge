package com.io.mesosphere

import com.io.mesosphere.buildingadt._


package object simulationadt {

  case class Dude(id: Int, call: Call, ticksCallAndLanding: Long = 0) {
    /*
      To track the ticks before dude reach the destination.
     */
    def tick(): Dude = {
      this.copy(ticksCallAndLanding = ticksCallAndLanding + 1)
    }
  }

  case class Call(from: Floor, to: Floor, direction: Direction)

  case object Call {
    def apply(from: Floor, to: Floor): Call = {
      val direction = if (from < to) Upstairs else if (from > to) Downstairs else Idle
      new Call(from, to, direction)
    }

  }

}
