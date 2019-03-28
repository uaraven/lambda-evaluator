package net.ninjacat.lambda.evaluator

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class AstTest {

    @Test
    fun testToString() {
        val func = Abstraction
            .of(
                Variable("s"), Variable("z")
            )
            .`as`(
                Variable("z")
            )

        assertThat(func.toString(), equalTo("λs.λz.z"))
    }

    @Test
    fun testNestedToString() {
        val f1 = Abstraction.of("y").`as`(
            Application.of(
                Variable("x"), Variable("z"),
                Variable("y")
            )
        )
        val func = Abstraction.of("x").`as`(f1)

        assertThat(func.toString(), equalTo("λx.λy.xzy"))
    }

    @Test
    fun testLambdaSimplification() {
        val lambda = Abstraction.of(Variable("a"), Variable("b"))
            .`as`(Application.of(Variable("a"), Variable("x"), Variable("b")))

        val expected = Abstraction.of(Variable("a")).`as`(
            Abstraction.of(Variable("b")).`as`(Application.of(Variable("a"), Variable("x"), Variable("b")))
        )

        assertThat(lambda.simplify(), equalTo(expected))
    }

    @Test
    fun testAlphaConversion() {
        val func = Abstraction.of("x").`as`(Application.of(Variable("x"), Variable("y")))
        val converted = func.alphaConversion()

        val expected = Abstraction.of("x'0'").`as`(Application.of(Variable("x'0'"), Variable("y"))) as Term

        assertThat(converted, equalTo(expected))
    }
}