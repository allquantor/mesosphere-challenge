package com.io.mesosphere


import scala.annotation.tailrec
import scala.collection.mutable
import buildingadt._
import com.io.mesosphere.scheduler.{ElevatorScheduler, FS}
import com.io.mesosphere.util.MyLogger
import simulationadt.{Dude, _}

object SimulationMain extends App with MyLogger with SimulationConfig {

  val building = Building(ElevatorsInBuilding, HighestFloorInBuilding)


  // The idea is that this queue could be periodically filled with new dudes and thus, represent a random stream of people.
  val queue = mutable.Queue[Dude]()

  val scheduler = new ElevatorScheduler(FloorsNinBuilding)

  // Some test cases to run a simple iteration.
  queue.enqueue(
    Dude(1, Call(Floor(1), Floor(3))),
    Dude(3, Call(Floor(4), Floor(0))),
    Dude(3, Call(Floor(4), Floor(0))),
    Dude(5, Call(Floor(5), Floor(-1))))



  def `simul@te`(building: Building): Unit = {
    log.info(s"Starting elevator simulation for the initial Building of: ${building}")

    @tailrec
    def simulationStep(building: Building, queue: mutable.Queue[Dude]): Building = {
      if (queue.nonEmpty || building.elevators.exists(_.path.nonEmpty)) {

        // update elevator state to current tick.
        val newElevatorState = building.elevators.map(_.tick())
        log.info(s"New elevator state ${newElevatorState}")

        // Get new jobs from the queue if available. Reject from == to.
        val currentJobs = queue.dequeueAll(d => d.call.from != d.call.to)
        log.info(s"New jobs for the simulation iteration ${currentJobs}")

        // For each job, represented as dude, calculate the FS value - pick the best elevator for job
        // assign the job to the elevator, update the elevator state, consider new job.
        val next: Seq[Elevator] = currentJobs.foldLeft(newElevatorState) { (accu, dude) =>

          val _elevatorStatsToJob: Seq[(Elevator, FS)] = accu.map { elevator =>
            (elevator, scheduler.compute(dude, elevator))
          }

          log.info(s"Scheduling Call:${dude.call} of Dude:${dude.id}. The Elevator FS calculated are:${_elevatorStatsToJob}")

          val (e, fs) = _elevatorStatsToJob.sortBy(_._2).last

          log.info(s"Elevator: ${e} seems to be the perfect match with FS:${fs}")

          val updatedElevator = e.addUpdateDude(dude)

          log.info(s"Call: ${dude.call} is added to Elevator: ${updatedElevator}")

          accu.filter(_.id != updatedElevator.id) :+ updatedElevator
        }
        simulationStep(building.copy(elevators = next), queue)
      } else {
        building
      }
    }

    simulationStep(building, queue)
  }

  this.`simul@te`(building)
}
