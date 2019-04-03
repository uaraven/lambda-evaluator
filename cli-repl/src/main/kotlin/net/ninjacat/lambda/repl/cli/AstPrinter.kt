package net.ninjacat.lambda.repl.cli

import net.ninjacat.lambda.evaluator.*
import org.fusesource.jansi.Ansi

class AstPrinter(private val root: Term) {

    fun printAst() {
        val ansi = Ansi()
        printTerm(ansi, root)
        println(ansi)
    }

    private fun printTerm(ansi: Ansi, t: Term) {
        when (t) {
            is Variable -> printTerm(ansi, t)
            is Abstraction -> printTerm(ansi, t)
            is Application -> printTerm(ansi, t)
            is Assignment -> printTerm(ansi, t)
        }
    }

    private fun printTerm(ansi: Ansi, v: Variable) {
        if (v.bindingIndex < 0) {
            ansi.fg(unboundColor).a(v.repr()).reset()
        } else {
            ansi.a(v.repr())
        }
    }

    private fun printTerm(ansi: Ansi, a: Assignment) {
        ansi.fg(boundNameColor).a(a.variable.repr()).a(" ")
            .fg(assignmentColor)
            .a(":=").a(" ")
            .reset()
        printTerm(ansi, a.value)
    }

    private fun printTerm(ansi: Ansi, lambda: Abstraction) {
        ansi.fg(lambdaColor).a("λ").reset().a(lambda.param.repr()).fg(lambdaColor).a(".").reset()
        printTerm(ansi, lambda.body)
    }

    private fun printTerm(ansi: Ansi, application: Application) {
        if (application.a is Variable || application.a is Application) {
            printTerm(ansi, application.a)
        } else {
            ansi.a("(")
            printTerm(ansi, application.a)
            ansi.a(")")
        }
        if (application.b is Variable) {
            printTerm(ansi, application.b as Variable)
        } else {
            ansi.a("(")
            printTerm(ansi, application.b)
            ansi.a(")")
        }
    }

    companion object {
        val lambdaColor = Ansi.Color.YELLOW
        val unboundColor = Ansi.Color.BLUE
        val boundNameColor = Ansi.Color.GREEN
        val assignmentColor = Ansi.Color.CYAN
    }
}