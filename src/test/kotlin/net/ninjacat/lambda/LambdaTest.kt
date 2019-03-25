package net.ninjacat.lambda

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Test

class LambdaTest {

    @Test
    fun testToString() {
        val func = Lambda(listOf('s','z'), listOf('z'))

        assertThat(func.toString(), equalTo("\uD835\uDF06sz.z"))
    }
}