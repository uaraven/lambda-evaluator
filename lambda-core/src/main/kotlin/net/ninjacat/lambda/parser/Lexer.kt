package net.ninjacat.lambda.parser

import java.io.Reader
import java.util.*
import kotlin.text.StringBuilder

enum class TokenType {
    LAMBDA,
    DOT,
    OPEN_PARENS,
    CLOSE_PARENS,
    ASSIGN,
    VARIABLE,
    EOF
}

data class Token(val type: TokenType, val value: String) {
    override fun toString(): String = "'$value'"

    companion object {
        val lambda = Token(TokenType.LAMBDA, "λ")
        val dot = Token(TokenType.DOT, "")
        val openParens = Token(TokenType.OPEN_PARENS, "(")
        val closeParens = Token(TokenType.CLOSE_PARENS, ")")
        val assign = Token(TokenType.ASSIGN, ":=")
        fun `var`(name: String) = Token(TokenType.VARIABLE, name)
        val eof: Token = Token(TokenType.EOF, "<eof>")
    }
}


class Lexer(private val reader: Reader) {

    private val buffer: Deque<Int> = LinkedList<Int>()

    fun tokenize(): Sequence<Token> {
        val tokens = mutableListOf<Token>()
        var c = readSkippingWhitespace()
        while (c != -1) {
            tokens.add(
                when (c.toChar()) {
                    '\\', 'λ' -> Token.lambda
                    '.' -> Token.dot
                    '(' -> Token.openParens
                    ')' -> Token.closeParens
                    ':' -> {
                        val next = readNext()
                        if (next.toChar() != '=') {
                            throw ParsingException("'=' expected, but '${next.toChar()}' found")
                        } else {
                            Token.assign
                        }
                    }
                    in 'a'..'z' -> Token.`var`(c.toChar().toString())
                    in 'A'..'Z' -> {
                        val name = StringBuilder()
                        while (c.toChar() in 'A'..'Z') {
                            name.append(c.toChar())
                            c = readNext()
                        }
                        putBack(c)
                        Token.`var`(name.toString())
                    }
                    else -> throw ParsingException("Unexpected token '${c.toChar()}'")
                }
            )
            c = readSkippingWhitespace()
        }
        tokens.add(Token.eof)
        return tokens.asSequence()
    }

    private fun readSkippingWhitespace(): Int {
        var c = readNext()
        while (whitespace.contains(c)) {
            c = readNext()
        }
        return c
    }

    private fun readNext(): Int = if (buffer.isNotEmpty()) buffer.pop() else reader.read()

    private fun putBack(c: Int) = buffer.push(c)

    companion object {
        private val whitespace = setOf(' ', '\t', '\n', '\r').map { it.toInt() }
    }
}