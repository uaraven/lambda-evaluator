package net.ninjacat.lambda.parser

import org.hamcrest.Matchers.equalTo
import org.junit.Test

import org.junit.Assert.*
import java.io.StringReader
import kotlin.math.exp

class TokenizerTest {

    @Test
    fun testVariable() {
        val reader = StringReader("xy")
        val tokenizer = Parser(reader)

        val term = tokenizer.tokenize()
        val expected = Group
            .of(Variable('x'), Variable('y')).simplify()

        assertThat(term, equalTo(expected))
    }


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

    @Test
    fun shouldParseYCombinator() {
        val reader = StringReader("λg.(λx.g(xx))(λx.g(xx))")
        val tokens = Parser(reader).tokenize()

        val lxg = Lambda.of(Variable('x')).`as`(
            Group.of(Variable('g'), Group.of(Variable('x'), Variable('x')))
        )

        val expected =
            Group.of(Lambda.of(Variable('g')).`as`(Group.of(lxg)), Group.of(lxg)) as Term

        assertThat(tokens, equalTo(expected))
    }
}