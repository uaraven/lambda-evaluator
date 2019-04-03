package net.ninjacat.lambda.repl.cli

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.beust.jcommander.JCommander
import org.slf4j.LoggerFactory


fun main(args: Array<String>) {
    val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    root.level = Level.OFF

    val connectionParameters = Parameters()
    val jc = JCommander.newBuilder()
        .programName("java -jar jcqlsh.jar")
        .addObject(connectionParameters)
        .build()
    jc.parse(*args)

    if (connectionParameters.showHelp) {
        jc.usage();
        return;
    }

    val repl = Repl(connectionParameters)
    repl.repl()
}