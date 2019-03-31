package net.ninjacat.lambda.evaluator

import net.ninjacat.lambda.parser.Parser
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class EvaluatorTest {

    @Test
    fun shouldEvaluateSimpleApplication() {
        val expr = Parser.parse("(\\x.x)a")
        val evaluator = Evaluator()

        val final = evaluator.eval(expr)

        assertThat(final, Matchers.hasSize(1))
        assertThat(final.first(), equalTo(Variable("a", -1) as Term))
    }

    @Test
    fun shouldEvaluateSimpleApplication2() {
        val expr = Parser.parse("(\\x.\\y.xy)(a)(b)")
        val evaluator = Evaluator()

        val final = evaluator.eval(expr)
        val expected: Term = Application(Variable("a", -1), Variable("b", -1))

        assertThat(final, Matchers.hasSize(2))
        assertThat(final.last(), equalTo(expected))
    }

    @Test
    fun shouldEvaluateLambda() {
        val expr = Parser.parse("(λxy.((λp.px)(λq.qy)))(c)(d)(e)")
        val evaluator = Evaluator()

        val final = evaluator.eval(expr)
        val expected: Term = Application(
            Application(Variable("c", -1),
            Variable("d", -1)), Variable("e", -1)
        )

        assertThat(final.last(), equalTo(expected))

    }
}