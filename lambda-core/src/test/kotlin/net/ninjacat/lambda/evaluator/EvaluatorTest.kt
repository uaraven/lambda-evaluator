package net.ninjacat.lambda.evaluator

import net.ninjacat.lambda.parser.Parser
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
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
}