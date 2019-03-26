package net.ninjacat.lambda.parser

import net.ninjacat.lambda.parser.Group
import net.ninjacat.lambda.parser.Lambda
import net.ninjacat.lambda.parser.Variable
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Test

class LambdaReprTest {

    @Test
    fun testToString() {
        val func = Lambda(
            listOf(Variable('s'), Variable('z')),
            listOf(Variable('z'))
        )

        assertThat(func.toString(), equalTo("λsz.z"))
    }

    @Test
    fun testNestedToString() {
        val f1 = Lambda(
            listOf(Variable('y')),
            listOf(
                Variable('x'),
                Variable('z'),
                Variable('y')
            )
        )
        val func = Lambda(
            listOf(Variable('x')),
            listOf(f1)
        )

        assertThat(func.toString(), equalTo("λx.λy.xzy"))
    }

    @Test
    fun testGroupToString() {
        val func = Lambda(
            listOf(Variable('x')),
            listOf(Variable('a'), Variable('x'))
        )

        val group = Group(
            listOf(
                func,
                Group.of(Variable('b'))
            )
        )

        assertThat(group.toString(), equalTo("(λx.ax(b))"))
    }
}