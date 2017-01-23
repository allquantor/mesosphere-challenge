package com.io.mesosphere.simulationadt


trait SimulationConfig {
  final val HighestFloorInBuilding = 5
  final val ElevatorsInBuilding = 3
  // With ground floor and basement.
  final val FloorsNinBuilding = HighestFloorInBuilding + 2
}
