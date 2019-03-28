package net.ninjacat.lambda.parser

import net.ninjacat.lambda.evaluator.*
import org.hamcrest.Matchers.equalTo
import org.junit.Test

import org.junit.Assert.*

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
        val expected = Abstraction
            .of(Variable("x"))
            .`as`(
                Variable("x")
            ).simplify() as Term

        assertThat(term, equalTo(expected))
    }

//    @Test
//    fun testVariableGroup() {
//        val term = Parser.parse("(xy)")
//        val expected = Group.of(
//            Variable("x"),
//            Variable("y")
//        ) as Term
//
//        assertThat(term, equalTo(expected))
//    }

    @Test
    fun testVariableApplication() {
        val term = Parser.parse("x(y)")
        val expected = Application(
            Variable("x"),
            Variable("y")
        ) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testSimpleLambda() {
        val term = Parser.parse("\\xy.xay")
        val expected = Abstraction
            .of(Variable("x"), Variable("y"))
            .`as`(
                Application.of(
                    Variable("x"),
                    Variable("a"),
                    Variable("y")
                )
            ).simplify() as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testLambdaApplication() {
        val term = Parser.parse("(λxy.xay)(a b)")

        val expected = Application(
            Abstraction
                .of(Variable("x"), Variable("y"))
                .`as`(
                    Application.of(Variable("x"), Variable("a"), Variable("y"))
                ).simplify(),
            Application(
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
            Abstraction
                .of(Variable("x"), Variable("y"))
                .`as`(
                    Variable("x")
                ).simplify()
        )

        assertThat(term, equalTo(expected as Term))
    }
}