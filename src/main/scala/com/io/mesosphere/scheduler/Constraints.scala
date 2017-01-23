package com.io.mesosphere.scheduler


import com.io.mesosphere.buildingadt.{Direction, Floor, Idle, Upstairs}
import com.io.mesosphere.simulationadt.{Call, SimulationConfig}


object Constraints extends SimulationConfig {

  private val calculateDeltaUpOrDown = (stateCall:Call) =>
    if (stateCall.direction == Upstairs) stateCall.from `Δ` Floor(ElevatorsInBuilding) else stateCall.from `Δ` Floor(-1)

  // Helper function to decide if the incoming call is not the same currentDirection as the state call.
  private[scheduler] val lastCallMovingFromLandingCall = (stateCall: Call, incomingCall: Call) => {
    val stateNodIdle = stateCall.direction != Idle
    val delta = calculateDeltaUpOrDown (stateCall)
    !delta.exists(_.nr == incomingCall.from.nr) || stateNodIdle
  }

  // Helper function to decide if the pick up floor is on the way of state call.
  private[scheduler] val pickupIsOnTheWay = (stateCall: Call, incomingCall: Call) => {
    val delta = calculateDeltaUpOrDown (stateCall)
    delta.exists(_.nr == incomingCall.from.nr)
  }

  private[scheduler] val inSameDirection =  (d1:Direction, d2:Direction) => d1 == d2

}
