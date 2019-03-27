package net.ninjacat.lambda.parser

import org.hamcrest.Matchers.equalTo
import org.junit.Test

import org.junit.Assert.*
import java.io.StringReader
import kotlin.math.exp

class ParserTest {

    @Test
    fun testVariable() {
        val reader = StringReader("x")
        val tokenizer = Parser(reader)

        val term = tokenizer.parse()
        val expected = Variable('x') as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun shouldParseIdentity() {
        val reader = StringReader("\\x.x")
        val tokenizer = Parser(reader)

        val term = tokenizer.parse()
        val expected = Lambda
            .of(Variable('x'))
            .`as`(
                Variable('x')
            ).simplify() as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testVariableGroup() {
        val reader = StringReader("xy")
        val tokenizer = Parser(reader)

        val term = tokenizer.parse()
        val expected = Group.of(Variable('x'), Variable('y')) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testVariableApplication() {
        val reader = StringReader("x(y)")
        val tokenizer = Parser(reader)

        val term = tokenizer.parse()
        val expected = Application(Variable('x'), Variable('y')) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testSimpleLambda() {
        val reader = StringReader("\\xy.xay")
        val tokenizer = Parser(reader)

        val term = tokenizer.parse()
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
        val reader = StringReader("\\xy.xay(a b)")
        val tokenizer = Parser(reader)
        val term = tokenizer.parse()

        val expected = Application(
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