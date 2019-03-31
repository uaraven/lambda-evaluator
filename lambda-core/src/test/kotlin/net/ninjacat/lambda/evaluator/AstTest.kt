package net.ninjacat.lambda.evaluator

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class AstTest {

    @Test
    fun testRepr() {
        val func = Abstraction
            .of("s", "z")
            .`as`(Variable("z", 0))

        assertThat(func.repr(), equalTo("λs.λz.z"))
    }

    @Test
    fun testDeBruijnRepr() {
        val func =Abstraction.of("x", "y", "z").`as`(
            Application.of(
                Application.of(Variable("x", 2), Variable("y", 1)),
                Variable("z", 0)
            )
        )

        assertThat(func.indexedRepr(), equalTo("λ λ λ 2 1 0"))
    }

    @Test
    fun testNestedRepr() {
        val f1 = Abstraction.of("y").`as`(
            Application.of(
                Variable("x", -1), Variable("z", -1),
                Variable("y", 0)
            )
        )
        val func = Abstraction.of("x").`as`(f1)

        assertThat(func.repr(), equalTo("λx.λy.xzy"))
    }

    @Test
    fun testLambdaBuilderUnwrapping() {
        val lambda = Abstraction.of("a", "b")
            .`as`(Application.of(Variable("a", 0), Variable("x", -1), Variable("b", 0)))

        val expected = Abstraction(
            Variable.parameter("a"),
            Abstraction(
                Variable.parameter("b"),
                Application.of(Variable("a", 0), Variable("x", -1), Variable("b", 0))
            )
        )

        assertThat(lambda, equalTo(expected))
    }

    @Test
    fun shouldShiftIndexOfVariable() {
        val x = Variable("x", 0)
        val shifted = x.shift(1, 0)
        assertThat(shifted.bindingIndex, `is`(1))
    }

    @Test
    fun shouldNotShiftIndexOfVariable() {
        val x = Variable("x", 0)
        val shifted = x.shift(1, 1)
        assertThat(shifted.bindingIndex, `is`(0))
    }

    @Test
    fun shouldShiftParametersInApplication() {
        val app = Application(
            Variable("x", 0),
            Variable("y", 0)
        )
        val shifted = app.shift(1, 0)

        val expected = Application(
            Variable("x", 1),
            Variable("y", 1)
        )
        assertThat(shifted, `is`(expected))
    }
}