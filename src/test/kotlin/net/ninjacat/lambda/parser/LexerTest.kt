package net.ninjacat.lambda.parser

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Test
import java.io.StringReader

class LexerTest {

    @Test
    fun shouldTokenizeAssignment() {
        val lexer = Lexer(StringReader("ID := \\x.x"))
        val tokens = lexer.tokenize().toList()
        val expected = listOf(
            Token.`var`("ID"),
            Token.assign,
            Token.lambda,
            Token.`var`("x"),
            Token.dot,
            Token.`var`("x"),
            Token.eof
        )
        assertThat(tokens, equalTo(expected))
    }

    @Test
    fun shouldTokenizeYCombinator() {
        val lexer = Lexer(StringReader(" λg.( λx.g(xx) ) ( λx.g(xx) )"))
        val tokens = lexer.tokenize().toList()
        val expected = listOf(
            Token.lambda,
            Token.`var`("g"),
            Token.dot,
            Token.openParens,
            Token.lambda,
            Token.`var`("x"),
            Token.dot,
            Token.`var`("g"),
            Token.openParens,
            Token.`var`("x"),
            Token.`var`("x"),
            Token.closeParens,
            Token.closeParens,
            Token.openParens,
            Token.lambda,
            Token.`var`("x"),
            Token.dot,
            Token.`var`("g"),
            Token.openParens,
            Token.`var`("x"),
            Token.`var`("x"),
            Token.closeParens,
            Token.closeParens,
            Token.eof
        )
        assertThat(tokens, equalTo(expected))
    }
}