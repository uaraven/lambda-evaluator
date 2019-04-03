package net.ninjacat.lambda.repl.cli

import org.fusesource.jansi.Ansi
import org.jline.reader.EndOfFileException

object CommandProcessor {
    private val commands = mapOf(
        Pair(":help", this::help),
        Pair(":exit", this::exit)
    )

    fun process(cmdLine: String) {
        val cmd = cmdLine.split("\\s+")
        if (commands.containsKey(cmd[0])) {
            commands[cmd[0]]?.invoke()
        }
    }
    private fun exit() {
        throw EndOfFileException()
    }

    private fun help() {
        val text = Ansi().a("λ calculus expression evaluator\n\n")
            .a("Variables, ").fgBrightDefault().a("one").reset().a(" character 'a'..'z' or ")
            .fgBrightDefault().a("multicharacter").reset().a(" uppercase ['A'..'Z']+\n")
            .a("For example ").fgBlue().a("x y").reset().a(" or ").fgBlue().a("ID CLCK")
            .reset().a(" are valid identifiers\n\n")
            .a("λ can be entered as '").fgBrightDefault().a("λ").reset()
            .a("' or as '").fgBrightDefault().a("\\").reset().a("'\n\n")
            .a("Any abstraction without free variables can be assigned to a name:\n")
            .a("  ").fgBlue().a("ID := \\x.x").reset().a(" or ").fgBlue()
            .a("t := λxy.x").reset().a("\n\n")
            .a("Other commands:\n")
            .fgCyan().a(":help\n:exit\n").reset()
        println(text)
    }

}