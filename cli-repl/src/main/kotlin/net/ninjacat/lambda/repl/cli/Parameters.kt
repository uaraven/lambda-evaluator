package net.ninjacat.lambda.repl.cli

import com.beust.jcommander.Parameter

class Parameters {
    @Parameter(names = ["--no-color"], description = "Do not use colored output")
    var noColor: Boolean = false
}