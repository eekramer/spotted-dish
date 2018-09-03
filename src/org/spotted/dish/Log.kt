package org.spotted.dish

import mu.KLogger
import java.time.LocalDateTime

abstract class LoggingClass constructor(private val logger: KLogger) {
    fun <T> log(invokable : ()->T , name: String) : T {
        logger.info("${LocalDateTime.now()} - Executing $name")
        val result = invokable()
        logger.info("${LocalDateTime.now()} - Exiting $name")
        return result
    }
}