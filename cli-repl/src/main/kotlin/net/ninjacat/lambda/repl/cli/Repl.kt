package net.ninjacat.lambda.repl.cli

import net.ninjacat.lambda.evaluator.Evaluator
import net.ninjacat.lambda.evaluator.Term
import net.ninjacat.lambda.parser.Parser
import net.ninjacat.lambda.parser.SyntaxException
import org.fusesource.jansi.Ansi
import org.jline.reader.*
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.io.IOException


class Repl(parameters: Parameters) {
    private val terminal = try {
        buildDefaultTerminal(parameters)
    } catch (ex: Exception) {
        buildFallbackTerminal()
    }

    private val reader = createLineReader(terminal)

    private val evaluator = Evaluator()

    @Throws(IOException::class)
    private fun createHistory(): History {
        val history = DefaultHistory()
        history.load()
        return history
    }

    private fun createParser(): org.jline.reader.Parser {
        val parser = DefaultParser()
        parser.escapeChars = charArrayOf()
        return parser
    }

    private fun isDumb(parameters: Parameters): Boolean {
        return System.console() == null || parameters.noColor
    }

    @Throws(IOException::class)
    private fun buildDefaultTerminal(parameters: Parameters): Terminal {
        return TerminalBuilder.builder()
            .dumb(isDumb(parameters))
            .jansi(true)
            .build()
    }

    @Throws(IOException::class)
    private fun buildFallbackTerminal(): Terminal {
        return TerminalBuilder.builder()
            .dumb(true)
            .system(false)
            .build()
    }

    private fun createLineReader(terminal: Terminal): LineReader {
        return LineReaderBuilder.builder()
            .terminal(terminal)
            .option(LineReader.Option.AUTO_GROUP, false)
            .option(LineReader.Option.AUTO_LIST, false)
            .option(LineReader.Option.AUTO_MENU, false)
            .history(createHistory())
            .parser(createParser())
            .appName("λ(λ)")
            .build()
    }

    fun repl() {
        while (true) {
            val line = try {
                val ln = this.reader.readLine(prompt)
                if (ln.trim().isEmpty()) {
                    continue
                }
                if (ln.trim().startsWith(":")) {
                    processCommand(ln)
                    continue
                }
                ln
            } catch (ignored: UserInterruptException) {
                continue
            } catch (ignored2: EndOfFileException) {
                break
            }

            val resultPair = try {
                val ast = Parser.parse(line)
                val termsToNormal = evaluator.eval(ast)
                Pair(ast, termsToNormal)
            } catch (ex: SyntaxException) {
                if (ex.position >= 0) {
                    println(line)
                    print(" ".repeat(ex.position - 1) + "^ ")
                }
                println(Ansi().fgBrightRed().a(ex.message).reset())
                continue
            }
            val ast = resultPair.first
            val steps = resultPair.second

            print(parsedPrompt)
            printAst(ast)
            printSteps(ast, steps)
        }
    }

    private fun processCommand(line: String) {
        CommandProcessor.process(line)
    }

    private fun printAst(ast: Term) {
        AstPrinter(ast).printAst()
    }

    private fun printSteps(ast: Term, steps: List<Term>) {
        if (!steps.isEmpty()) {
            print(resultPrompt)
            printAst(steps.last())
        }
        print(stepsPrompt)
        if (steps.isEmpty()) {
            print("    ")
            printAst(ast)
        } else {
            steps.forEach {
                print("    ")
                printAst(it)
            }
        }
    }

    companion object {
        const val prompt = " > "
        const val parsedPrompt = "   Parsed expression: "
        const val resultPrompt = "Evaluated expression: "
        const val stepsPrompt = " Steps to normal form:\n"
    }
}