package net.ninjacat.lambda

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Test

class LambdaReprTest {

    @Test
    fun testToString() {
        val func = Lambda(
            listOf(Variable('s'), Variable('z')),
            Variable('z')
        )

        assertThat(func.toString(), equalTo("\uD835\uDF06sz.z"))
    }

    @Test
    fun testNestedToString() {
        val f1 = Lambda(listOf(Variable('y')),
            Group(listOf(Variable('x'), Variable('z'), Variable('y'))).simplify())
        val func = Lambda(
            listOf(Variable('x')),
            f1
        )

        assertThat(func.toString(), equalTo("\uD835\uDF06x.\uD835\uDF06y.xzy"))
    }

    @Test
    fun testGroupToString() {
        val func = Lambda(
            listOf(Variable('x')),
            Group(listOf(Variable('a'), Variable('x')))
        )

        val group = Group(listOf(func, Variable('b')))

        assertThat(group.toString(), equalTo("((\uD835\uDF06x.ax)b)"))
    }
}