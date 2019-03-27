package net.ninjacat.lambda.evaluator

import net.ninjacat.lambda.evaluator.Lambda
import net.ninjacat.lambda.evaluator.Variable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class LambdaTest {

    @Test
    fun testToString() {
        val func = Lambda
            .of(
                Variable("s"), Variable("z")
            )
            .`as`(
                Variable("z")
            )

        assertThat(func.toString(), equalTo("λsz.z"))
    }

    @Test
    fun testNestedToString() {
        val f1 = Lambda.of(
            Variable("y")).`as`(
                Variable("x"),
                Variable("z"),
                Variable("y")

        )
        val func = Lambda.of(Variable("x")).`as`(f1)

        assertThat(func.toString(), equalTo("λx.λy.xzy"))
    }

    @Test
    fun testLambdaSimplification() {
        val lambda = Lambda.of(Variable("a"), Variable("b"))
            .`as`(Variable("a"), Variable("x"), Variable("b"))

        val expected = Lambda.of(Variable("a")).`as`(
            Lambda.of(Variable("b")).`as`(Variable("a"), Variable("x"), Variable("b"))
        )

        assertThat(lambda.simplify(), equalTo(expected))
    }
}