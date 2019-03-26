package net.ninjacat.lambda.parser

import org.hamcrest.Matchers.equalTo
import org.junit.Test

import org.junit.Assert.*
import java.io.StringReader

class TokenizerTest {

    @Test
    fun testSimpleLambda() {
        val reader = StringReader("\\xy.xay")
        val tokenizer = Parser(reader)

        val term = tokenizer.tokenize()
        val expected = Lambda
            .of(Variable('x'), Variable('y'))
            .`as`(
                Variable('x'),
                Variable('a'),
                Variable('y')
            ).simplify() as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testLambdaApplication() {
        val reader = StringReader("(\\xy.xay)(a b)")
        val tokenizer = Parser(reader)
        val term = tokenizer.tokenize()

        val expected = Group.of(
            Lambda
                .of(Variable('x'), Variable('y'))
                .`as`(
                    Variable('x'),
                    Variable('a'),
                    Variable('y')
                ).simplify(),
            Group.of(
                Variable('a'),
                Variable('b')
            )
        )

        assertThat(term, equalTo(expected as Term))
    }
}