package com.io.mesosphere

import com.io.mesosphere.simulationadt._
import com.io.mesosphere.util.MyLogger



package object buildingadt {

  sealed trait Direction

  case object Upstairs extends Direction

  case object Downstairs extends Direction

  case object Idle extends Direction

  case class Floor(nr: Int) extends Ordered[Floor] {

    private val computeDeltaLevels = (that: Floor) => {
      // This is absolute ugly and not idiomatic and should be changed.
      val step = (this,that) match {
        case (thiz,thad) if thiz.nr < 0 && thad.nr <0 && thiz < thad => 1
        case (thiz,thad) if thiz.nr < 0 && thad.nr <0 && thad > thad => -1
        case (thiz,thad) if thiz > thad => -1
        case (thiz,thad) if thiz < thad => 1
        case (thiz,thad) if thiz == thad => 1
      }
      this.nr to that.nr by step
    }

    def `Δ`(that: Floor): Seq[Floor] = computeDeltaLevels(that) map Floor

    override def compare(that: Floor): Int = {
      this.nr - that.nr
    }
  }


  case class Elevator(id: Int,
                      currentState: Floor,
                      dudes: Seq[Dude],
                      currentDirection: Direction,
                      path: Seq[(Floor,Dude)]
                     )
    extends MyLogger {

    /**
      * Add a new or update a dude in the elevator.
      *
      * @param d dude to update
      * @return updated elevator state
      */

    def addUpdateDude(d: Dude): Elevator = {
      val newPath =  expandThePath(d)
      val e = this.copy(
        path = newPath,
        dudes = dudes.filter(_.id != d.id) :+ d
      )
      val direction = if (currentState < newPath.head._1) Upstairs else if (currentState > newPath.head._1) Downstairs else Idle
      e.copy(currentDirection = direction)
    }

    private def expandThePath(d: Dude): Seq[(Floor,Dude)] = {
      val deltaForPickup = path.lastOption.map(_._1).getOrElse(currentState)`Δ`(d.call.from)
      val pickupPath = if (deltaForPickup.size == 1) deltaForPickup else deltaForPickup.drop(1)
      val deliverPath = pickupPath.last.`Δ`(d.call.to).drop(1)
      val toAdd = pickupPath ++ deliverPath
      path ++ (toAdd).zip(Array.fill(toAdd.size)(d))
    }

    private def goalDest(path:Seq[(Floor,Dude)],d:Dude):Boolean = {
      path.size <= 1 || !path.tail.exists(_._2.id == d.id)
    }

    def tick(): Elevator = {
      this match {
        case Elevator(_, state, currentDude :: restDudes, _, elevatorPath) =>
          // reached the destination
          if (currentDude.call.to == state && goalDest(elevatorPath,currentDude)) {
            log.info(s"${Array.fill(50)("-").mkString("")}")
            log.info(s"Another happy customer: ${currentDude}")
            log.info(s"${Array.fill(50)("-").mkString("")}")
            // set the direction for the next guest
            val nextDirection = elevatorPath.headOption.map{case (nextFloor,d)  =>
              if (currentState < nextFloor) Upstairs else if (currentState > nextFloor) Downstairs else Idle
            }.getOrElse(Idle)

            this.copy(
              currentDirection = nextDirection,
              dudes = restDudes,
              // last task in path
              path = if(elevatorPath.isEmpty) elevatorPath else elevatorPath.tail
            )
          } else {
            this.copy(
              currentState = elevatorPath.head._1,
              dudes = currentDude.tick() :: restDudes,
              path = elevatorPath.tail)
          }
        case Elevator(_, _, _, Idle, _) => this
        case _ => throw new Exception("This state should never been reached!!!")

      }

    }
  }

  // This entity represent the building
  case class Building(elevators: Seq[Elevator], floors: Seq[Floor])

  case object Building {

    val BasementFloor = Floor(-1)
    val GroundFloor = Floor(0)

    /**
      *
      * @param elevators Amount of elevators in the building.
      * @param floors    Amount of floors + Basement + Ground (floors + 2).
      * @return Building model.
      */

    def apply(elevators: Int,
              floors: Int): Building = {

      val upperFloor = Floor(floors)
      val _floors = BasementFloor `Δ` upperFloor

      val _elevators = 1 to elevators map (Elevator(_, GroundFloor, Nil, Idle, Nil))

      new Building(_elevators, _floors)
    }
  }
}
