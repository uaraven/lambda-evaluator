package net.ninjacat.lambda.parser

import net.ninjacat.lambda.evaluator.*
import org.hamcrest.Matchers.equalTo
import org.junit.Test

import org.junit.Assert.*
import java.io.StringReader

class ParserTest {

    @Test
    fun testVariable() {
        val term = Parser.parse("x")
        val expected = Variable("x", -1) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun shouldParseIdentity() {
        val term = Parser.parse("\\x.x")
        val expected = Abstraction
            .of("x")
            .`as`(
                Variable("x", 0)
            ) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testVariableApplication() {
        val term = Parser.parse("x(y)")
        val expected = Application(
            Variable("x", -1),
            Variable("y", -1)
        ) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testSimpleLambda() {
        val parser = Parser(Lexer(StringReader("\\xy.xay")).tokenize())
        val term = parser.parse()
        val expected = Abstraction
            .of("x", "y")
            .`as`(
                Application.of(
                    Variable("x", 1),
                    Variable("a", -1),
                    Variable("y", 0)
                )
            ) as Term

        assertThat(term, equalTo(expected))
    }

    @Test
    fun testSimpleLambdaApplication() {
        val term = Parser.parse("(λx.x)a")

        val expected = Application(
            Abstraction
                .of("x")
                .`as`(
                    Application.of(
                        Variable("x", 0)
                    )
                ),
            Variable("a", -1)
        )

        assertThat(term, equalTo(expected as Term))
    }

    @Test
    fun testLambdaApplication() {
        val term = Parser.parse("(λxy.xay)(a b)")

        val expected = Application(
            Abstraction
                .of("x", "y")
                .`as`(
                    Application.of(
                        Variable("x", 1),
                        Variable("a", -1),
                        Variable("y", 0)
                    )
                ),
            Application(
                Variable("a", -1),
                Variable("b", -1)
            )
        )

        assertThat(term, equalTo(expected as Term))
    }

    @Test
    fun testLambdaAssignment() {
        val term = Parser.parse("ID := λxy.x")

        val expected = Assignment(
            Variable("ID", -1),
            Abstraction
                .of("x", "y")
                .`as`(
                    Variable("x", 1)
                )
        )

        assertThat(term, equalTo(expected as Term))
    }

    @Test
    fun testDeBruijnIndex() {
        val term = Parser.parse("(\\xyz.xyz)a")
        val expected = Application.of(
            Abstraction.of("x", "y", "z").`as`(
                Application.of(
                    Application.of(Variable("x", 2), Variable("y", 1)),
                    Variable("z", 0)
                )
            ), Variable.parameter("a")
        )

        assertThat(term, equalTo(expected))
    }

    @Test(expected = ParsingException::class)
    fun shouldFailExpectingVariable() {
        Parser.parse("\\(x).x")
    }

    @Test(expected = ParsingException::class)
    fun shouldFailExpectingDot() {
        val tokens = listOf(
            Token.lambda,
            Token.`var`("x"),
            Token.`var`("p"),
            Token.dot,
            Token.`var`("x")
        )
        Parser(tokens.asSequence()).parse()
    }

}