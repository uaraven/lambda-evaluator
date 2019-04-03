package net.ninjacat.lambda.repl.cli

import com.beust.jcommander.Parameter

class Parameters {
    @Parameter(names = ["-n", "--no-color"], description = "Do not use colored output")
    var noColor: Boolean = false

    @Parameter(names = ["-h", "--help"], description = "Show help message")
    var showHelp: Boolean = false
}