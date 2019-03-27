package net.ninjacat.lambda.parser

import org.hamcrest.Matchers.equalTo
import org.junit.Test

import org.junit.Assert.*
import java.io.StringReader
import kotlin.math.exp

class ParserTest {

    @Test
    fun testVariable() {
        val term = Parser.parse("x")
        val expected = Variable("x") as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun shouldParseIdentity() {
        val term = Parser.parse("\\x.x")
        val expected = Lambda
            .of(Variable("x"))
            .`as`(
                Variable("x")
            ).simplify() as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testVariableGroup() {
        val term = Parser.parse("(xy)")
        val expected = Group.of(Variable("x"), Variable("y")) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testVariableApplication() {
        val term = Parser.parse("x(y)")
        val expected = Application(Variable("x"), Variable("y")) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testSimpleLambda() {
        val term = Parser.parse("\\xy.xay")
        val expected = Lambda
            .of(Variable("x"), Variable("y"))
            .`as`(
                Variable("x"),
                Variable("a"),
                Variable("y")
            ).simplify() as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testLambdaApplication() {
        val term = Parser.parse("(λxy.xay)(a b)")

        val expected = Application(
            Lambda
                .of(Variable("x"), Variable("y"))
                .`as`(
                    Variable("x"),
                    Variable("a"),
                    Variable("y")
                ).simplify(),
            Group.of(
                Variable("a"),
                Variable("b")
            )
        )

        assertThat(term, equalTo(expected as Term))
    }

    @Test
    fun testLambdaAssignment() {
        val term = Parser.parse("ID := λxy.x")

        val expected = Assignment(
            Variable("ID"),
            Lambda
                .of(Variable("x"), Variable("y"))
                .`as`(
                    Variable("x")
                ).simplify()
        )

        assertThat(term, equalTo(expected as Term))
    }
}