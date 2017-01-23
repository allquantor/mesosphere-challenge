
<center>
     <h2>Mesosphere</h2> 
     <h3>Distributed Applications Engineer Challenge</h3>
</center>
 
**Assignment:**

Design and implement an elevator control system. What data structures, interfaces and algorithms will you need? Your elevator control system should be able to handle a few elevators — up to 16.

You can use the language of your choice to implement an elevator control system. In the end, your control system should provide an interface for:

Querying the state of the elevators (what floor are they on and where they are going),
receiving an update about the status of an elevator,
receiving a pickup request,
time-stepping the simulation.
For example, we could imagine in Scala an interface like this:

```
trait ElevatorControlSystem {
    def status(): Seq[(Int, Int, Int)]
    def update(Int, Int, Int)
    def pickup(Int, Int)
    def step()
}
```

Here we have chosen to represent elevator state as 3 integers:

Elevator ID, Floor Number, Goal Floor Number

A pickup request is two integers:

Pickup Floor, Direction (negative for down, positive for up)

This is not a particularly nice interface, and leaves some questions open. For example, the elevator state only has one goal floor; but it is conceivable that an elevator holds more than one person, and each person wants to go to a different floor, so there could be a few goal floors queued up. Please feel free to improve upon this interface!

The most interesting part of this challenge is the scheduling problem. The simplest implementation would be to serve requests in FCFS (first-come, first-served) order. This is clearly bad; imagine riding such an elevator! Please discuss how your algorithm improves on FCFS in your write-up.


**Result:**

* To run:

`sbt run`

* Scheduling Algorithm:

NC (Nearest Car)
```
1. FS = N + 1 - (d - 1) = N + 2 - d
This rule will come into effect if the elevator car is moving towards the landing
call and the call is set in the same direction.
2. FS = N + 1 - d
This rule will come into effect if the elevator car is moving towards the landing
call but the call is set to the opposite direction.
3. FS = 1
This rule will come into effect if the elevator car is already moving away from
the landing call (the elevator is responding to some other call).
4. FS = N + 1 - d
This rule will come into effect if the elevator car is idle.
```

Taken from:
```
 Constructing a Scheduling Algorithm
 For Multidirectional Elevators
 JOAKIM EDLUND, FREDRIK BERNTSSON
 http://www.diva-portal.org/smash/get/diva2:811554/FULLTEXT01.pdf
 ```
(Please see a note to scheduling at the end of this documentation.)

**Model**:

`Building(elevators: Seq[Elevator], floors: Seq[Floor])`

Represent any building with N elevators and N floors.

` Dude(id: Int, call: Call, ticksCallAndLanding: Long = 0) `

Represent a person using the elevator with a particular call 

` Call(from: Floor, to: Floor, direction: Direction)`

that describe the task for an elevator.

`SimulationMain.scala`

Do contain the simulation process of getting `Dude` using the `ElevatorScheduler`
for scheduling the jobs to the right elevators, updating the state of the building.

`The whole simulation is is based on ticks describing one time unit in that an elevator can change the state +1 -1 floor.`

* Querying the state of the elevators (what floor are they on and where they are going)

This is possible by getting the state of the Elevator entity.
  ```
  case class Elevator(id: Int,
                       currentState: Floor,
                       dudes: Seq[Dude],
                       currentDirection: Direction,
                       path: Seq[(Floor,Dude)]) // the tasks for the elevator

```

* This is not a particularly nice interface, and leaves some questions open. For example, the elevator state only has one goal floor; but it is conceivable that an elevator holds more than one person, and each person wants to go to a different floor, so there could be a few goal floors queued up. Please feel free to improve upon this interface!

Each elevator have a path which describe his further journey. Is is expanded after a `Dude` is assigned to the elevator.
Note: This is also considered by the calculation of the `FS` value. If we have more than one `Dude` in our queue
we calculation the `FS` value with the LAST state for the elevator and not the current state the elevator have when the calculation is performed.

* Receiving an update about the status of an elevator

This is realized in the main simulation, after the Elevator entity perform `tick()`
the status is updated. A log statement do print the updated state. It could also be
 send somewhere else.


* Receiving a pickup request

The  scheduler `ElevatorScheduler` calculate the values `FS` to decide which elevator should pick up 
 the next available task. The interface in the Elevator entity is `addUpdateDude(d:Dude)`

* Time-stepping the simulation.

The Scheduling is deterministic. The state of the entity `Building` describe
 a state for an iteration step. By reproducing the state - we are able to time-stepping
 the simulation.
 

Note to the Scheduling:

This approach should outperform the FCFS approach since it consider the distance 
between the call from floor and the state floor and the constraints:
1. If the call  is set from the same direction where the elevator is going.
2. If the call is set to the same direction where the elevator is going.
3. If the elevator is in status Idle.

The elevator scheduling problem is a subset of the job scheduling problem 
<https://en.wikipedia.org/wiki/Job_shop_scheduling> 

If the goal is to find the global optima we first have to decide what 
we want to maximize is it efficiency or the customer journey. 

Example: 
```
Elevator A = with call from Floor(1) to Floor(3)
Elevator B = with Idle on Floor(2)
Incoming call from Floor(0) to Floor(-1)
```

Efficiency would mean we send the elevator A but this is not really cool if you are
 the guest who want to go to Floor(3).


