package com.io.mesosphere.scheduler

import com.io.mesosphere.buildingadt._
import com.io.mesosphere.simulationadt._

class ElevatorScheduler(N: Int) {

  import Constraints._


  // d for Distance. Algorithm related.
  private val d = (state: Floor, to: Floor) =>
    Math.max(state.nr, to.nr) - Math.min(state.nr, to.nr)

  /*
    1. FS = N + 1 - (direction - 1) = N + 2 - direction
        This rule will come into effect if the elevator car is moving towards the landing
        call and the call is set in the same currentDirection.
   */

  private val inTheSameDirection: PartialFunction[(Dude, Elevator), FS] = {
    case (
      dude@Dude(_, Call(from, to, direction), _),
      Elevator(_, state, dudes, _, path)
      ) if pickupIsOnTheWay(dudes.lastOption.getOrElse(dude).call, dude.call)
      // set in the same direction
      && inSameDirection(direction, dudes.lastOption.getOrElse(dude).call.direction) =>
      N + 2 - d(dudes.lastOption.getOrElse(dude).call.to, to)
  }


  /*
     2. FS = N + 1 - direction
          This rule will come into effect if the elevator car is moving towards the landing
          call but the call is set to the opposite currentDirection.
   */


  private val notInTheSameDirection: PartialFunction[(Dude, Elevator), FS] = {
    case (
      dude@Dude(_, Call(from, to, direction), _),
      Elevator(_, state, dudes, _, path)
      ) if pickupIsOnTheWay(dudes.lastOption.getOrElse(dude).call, dude.call)
      // set NOT in the same direction
      && !inSameDirection(direction, dudes.lastOption.getOrElse(dude).call.direction) =>
      N + 1 - d(dudes.lastOption.getOrElse(dude).call.to, to)
  }


  /*
          3. FS = 1
          This rule will come into effect if the elevator car is already moving away from
          the landing call (the elevator is responding to some other call).

          Note: 1 is static, this however could be optimized if the factor
          of "how far would the elevator need to go not in the call direction"
          would be considered. However, this would maximize the efficiency but not
          the customer journey.
   */

  private val away: PartialFunction[(Dude, Elevator), FS] = {
    case (
      dude@Dude(_, Call(from, to, direction), _),
      Elevator(_, state, dudes, _, _)
      ) if Constraints.lastCallMovingFromLandingCall
    (dudes.lastOption.getOrElse(dude).call, dude.call)
    => 1
  }

  /*
       4. FS = N + 1 - direction
          This rule will come into effect if the elevator car is idle.
   */

  private val idle: PartialFunction[(Dude, Elevator), FS] = {
    case (
      Dude(_, Call(_, to, _), _),
      Elevator(_, state, dudes, Idle, _)
      ) if dudes.isEmpty =>
      N + 1 - d(state, to)
  }

  private val unhandled: PartialFunction[(Dude, Elevator), FS] = {
    case (dude, e) => throw new IllegalStateException(s"This state should never been reached for ${(dude, e)}!")
  }


  def compute(dude: Dude, elevator: Elevator): FS = {
    Seq((dude, elevator)).map {
      idle orElse
        inTheSameDirection orElse
        notInTheSameDirection orElse
        away orElse
        unhandled
      // this won't cause a side effect because unhandled will already cause one :P
    }.head
  }


}
