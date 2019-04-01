package net.ninjacat.lambda.repl.cli

import net.ninjacat.lambda.evaluator.Evaluator
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.io.IOException
import org.jline.reader.impl.history.DefaultHistory
import org.jline.reader.History
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import net.ninjacat.lambda.evaluator.Term
import net.ninjacat.lambda.parser.Parser
import net.ninjacat.lambda.parser.SyntaxException
import org.fusesource.jansi.Ansi


class Repl(parameters: Parameters) {
    private val terminal = try {
        buildDefaultTerminal(parameters)
    } catch (ex: Exception) {
        buildFallbackTerminal()
    }
    private val history = createHistory()

    private val reader = LineReaderBuilder.builder()
        .option(LineReader.Option.AUTO_REMOVE_SLASH, false)
        .terminal(terminal)
        .history(this.history)
        .build()

    private val evaluator = Evaluator()

    @Throws(IOException::class)
    private fun createHistory(): History {
        val history = DefaultHistory()
        history.load()
        return history
    }

    private fun isDumb(parameters: Parameters): Boolean {
        return System.console() == null || parameters.noColor
    }

    @Throws(IOException::class)
    private fun buildDefaultTerminal(parameters: Parameters): Terminal {
        return TerminalBuilder.builder()
            .dumb(isDumb(parameters))
            .build()
    }

    @Throws(IOException::class)
    private fun buildFallbackTerminal(): Terminal {
        return TerminalBuilder.builder()
            .dumb(true)
            .system(false)
            .build()
    }

    fun repl() {
        while (true) {
            val line = try {
                this.reader.readLine(prompt)
            } catch (ignored: UserInterruptException) {
                continue
            }
            val resultPair = try {
                val ast = Parser.parse(line)
                val termsToNormal = evaluator.eval(ast)
                Pair(ast, termsToNormal)
            } catch (ex: SyntaxException) {
                if (ex.position >= 0) {
                    println(line)
                    println(" ".repeat(ex.position - 1) + "^")
                }
                println(Ansi().fgBrightRed().a(ex.message).reset())
                continue
            }
            val ast = resultPair.first
            val steps = resultPair.second
            printAst(ast)
            printSteps(ast, steps)
        }
    }

    private fun printAst(ast: Term) {
        println()
        AstPrinter(ast).printAst()
    }

    private fun printSteps(ast: Term, steps: List<Term>) {
        println("Steps to normal form:")
        if (steps.isEmpty()) {
            printAst(ast)
        } else {
            steps.forEach { printAst(it) }
        }
    }

    companion object {
        val prompt = " > "
    }
}