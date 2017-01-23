package com.io.mesosphere


import scala.collection.mutable


object SimulationMain extends App {

  case class Building(elevators: List[Elevator], floors: Int)

  case class Floor(f: Int)

  case class Call(from: Floor, to: Floor)

  case class Dude(c: Call)

  case class Elevator(state: Floor, customers: List[Dude], jobs:List[Call])

  val building = _

  val queue = mutable.Queue[Dude]()

  def changeElevatorState(e: Elevator): Elevator = _

  def scheduleAlg(job: Dude, elevators: List[Elevator]): (Elevator, Call) = _

  def updateWithOrder(accu: List[Elevator], e: Elevator, dudes: Call): Elevator = _

  //todo: A known issue is that the dudes are not updated yet (waiting time) this should be done somewhere!!!
  def `simul@te`(building: Building): Unit = {

    def simulationStep(building: Building, queue: mutable.Queue[Dude]): Building = {
      if (queue.nonEmpty || building.elevators.exists(_.jobs.nonEmpty)) {

        // Here is the tick.
        // This is the first step, they should change a position Up or Down or Idle
        val newElevatorState = building.elevators.map(changeElevatorState)

        // Get the current jobs to proceed in this tick.
        val currentJobs = queue.dequeueAll(_.isInstanceOf[Dude])

        // Here the actual scheduling should happen. The result should be the current state + updated jobs for next tick.
        val next: List[Elevator] = currentJobs.foldLeft(List[Elevator]()) { (accu, job) =>
          val (e, dudes) = scheduleAlg(job, accu)
          val updatedElevator = updateWithOrder(accu, e, dudes)
          accu :+ updatedElevator
        }

        // again
        simulationStep(building.copy(elevators = next), queue)
      } else {
        building
      }
    }

  }

  this.`simul@te`(building)
}
