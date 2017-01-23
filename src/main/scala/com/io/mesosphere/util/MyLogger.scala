package com.io.mesosphere.util

import java.util.logging.Logger

/**
  * Created by ivan on 23/01/2017.
  */
trait MyLogger {
  val log : Logger = Logger.getLogger("Elevat0r")
}
