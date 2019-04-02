package net.ninjacat.lambda.parser

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.StringReader

class LexerTest {

    @Test
    fun shouldTokenizeAssignment() {
        val lexer = Lexer(StringReader("ID := \\x.x"))
        val tokens = lexer.tokenize().toList()
        val expected = listOf(
            Token.`var`("ID"),
            Token.assign(),
            Token.lambda(),
            Token.`var`("x"),
            Token.dot(),
            Token.`var`("x"),
            Token.eof
        )
        assertThat(tokens, equalTo(expected))
    }

    @Test
    fun shouldUnwrapMultiParameterAbstraction() {
        val lexer = Lexer(StringReader(" λxy.xy"))
        val tokens = lexer.tokenize().toList()
        val expected = listOf(
            Token.lambda(),
            Token.`var`("x"),
            Token.dot(),
            Token.lambda(),
            Token.`var`("y"),
            Token.dot(),
            Token.`var`("x"),
            Token.`var`("y"),
            Token.eof
        )
        assertThat(tokens, equalTo(expected))
    }

    @Test
    fun shouldUnwrapSeveralMultiParameterAbstractions() {
        val lexer = Lexer(StringReader("λxyz.λab.c"))
        val tokens = lexer.tokenize().toList()
        val expected = listOf(
            Token.lambda(),
            Token.`var`("x"),
            Token.dot(),
            Token.lambda(),
            Token.`var`("y"),
            Token.dot(),
            Token.lambda(),
            Token.`var`("z"),
            Token.dot(),
            Token.lambda(),
            Token.`var`("a"),
            Token.dot(),
            Token.lambda(),
            Token.`var`("b"),
            Token.dot(),
            Token.`var`("c"),
            Token.eof
        )
        assertThat(tokens, equalTo(expected))
    }

    @Test
    fun shouldTokenizeYCombinator() {
        val lexer = Lexer(StringReader("λg.( λx.g(xx) ) ( λx.g(xx) )"))
        val tokens = lexer.tokenize().toList()
        val expected = listOf(
            Token.lambda(),
            Token.`var`("g"),
            Token.dot(),
            Token.openParens(),
            Token.lambda(),
            Token.`var`("x"),
            Token.dot(),
            Token.`var`("g"),
            Token.openParens(),
            Token.`var`("x"),
            Token.`var`("x"),
            Token.closeParens(),
            Token.closeParens(),
            Token.openParens(),
            Token.lambda(),
            Token.`var`("x"),
            Token.dot(),
            Token.`var`("g"),
            Token.openParens(),
            Token.`var`("x"),
            Token.`var`("x"),
            Token.closeParens(),
            Token.closeParens(),
            Token.eof
        )
        assertThat(tokens, equalTo(expected))
    }

    @Test
    fun shouldTokenizeLongNamesInLambda() {
        val lexer = Lexer(StringReader(" λaXX.XXa"))
        val tokens = lexer.tokenize().toList()
        val expected = listOf(
            Token.lambda(),
            Token.`var`("a"),
            Token.dot(),
            Token.lambda(),
            Token.`var`("XX"),
            Token.dot(),
            Token.`var`("XX"),
            Token.`var`("a"),
            Token.eof
        )
        assertThat(tokens, equalTo(expected))
    }

    @Test(expected = LexerException::class)
    fun shouldFailOnInvalidAssignment() {
        Lexer(StringReader("ID = \\x.x")).tokenize()
    }

    @Test(expected = LexerException::class)
    fun shouldFailOnInvalidAssignment2() {
        Lexer(StringReader("ID : \\x.x")).tokenize()
    }

}