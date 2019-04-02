package net.ninjacat.lambda.parser

import java.io.Reader
import java.util.*

/**
 * Valid tokens
 */
enum class TokenType {
    LAMBDA,
    DOT,
    OPEN_PARENS,
    CLOSE_PARENS,
    ASSIGN,
    VARIABLE,
    EOF
}

/**
 * Token produced by Lexer
 */
data class Token(val type: TokenType, val value: String, val position: Int) {
    override fun toString(): String = "'$value'"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Token

        if (type != other.type) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }


    companion object {
        fun lambda(pos: Int = -1) = Token(TokenType.LAMBDA, "λ", pos)
        fun dot(pos: Int = -1) = Token(TokenType.DOT, ".", pos)
        fun openParens(pos: Int = -1) = Token(TokenType.OPEN_PARENS, "(", pos)
        fun closeParens(pos: Int = -1) = Token(TokenType.CLOSE_PARENS, ")", pos)
        fun assign(pos: Int = -1) = Token(TokenType.ASSIGN, ":=", pos)
        fun `var`(name: String, pos: Int = -1) = Token(TokenType.VARIABLE, name, pos)
        val eof: Token = Token(TokenType.EOF, "<eof>", -1)
    }
}

/**
 * Holds current state of lambda parameter parsing, allowing to normalize "multiparameter" lambda expressions
 *
 * λxyz.xyz -> λx.λy.λz.xyz
 */
private data class LambdaUnwrapperState(
    private var inLambda: Boolean,
    private var paramCount: Int,
    private var expectingDot: Boolean
) {

    fun startLambda() {
        inLambda = true
        expectingDot = false
        paramCount = 0
    }

    fun endLambda() {
        inLambda = false
        expectingDot = false
        paramCount = 0
    }

    fun addParameter() {
        if (inLambda) {
            paramCount += 1
            expectingDot = true
        }
    }

    fun isSecondParameter() = inLambda && paramCount > 0

    fun clearDot() {
        expectingDot = false
    }

    fun generateDot() = inLambda && expectingDot

    companion object {
        fun new() = LambdaUnwrapperState(false, 0, false)
    }
}

/**
 * Splits input stream into a sequence of [Token]s.
 *
 * Performs rewriting of "multiparameter" λ abstractions to simplify later parsing.
 */
class Lexer(private val reader: Reader) {

    private val buffer: Deque<Int> = LinkedList<Int>()
    private var position = 0

    fun tokenize(): Sequence<Token> {

        val tokens = mutableListOf<Token>()
        val luState = LambdaUnwrapperState.new()
        var c = readSkippingWhitespace()
        while (c != -1) {
            if (luState.generateDot() && c.toChar() != '.') {
                tokens.add(Token.dot(position))
                luState.clearDot()
            }
            tokens.add(
                when (c.toChar()) {
                    'λ' -> {
                        luState.startLambda()
                        Token.lambda(position)
                    }
                    '.' -> {
                        luState.endLambda()
                        Token.dot(position)
                    }
                    '(' -> {
                        luState.endLambda()
                        Token.openParens(position)
                    }
                    ')' -> {
                        luState.endLambda()
                        Token.closeParens(position)
                    }
                    ':' -> {
                        val next = readNext()
                        if (next.toChar() != '=') {
                            throw LexerException("'=' expected, but '${next.toChar()}' found", position)
                        } else {
                            Token.assign(position)
                        }
                    }
                    in 'a'..'z' -> {
                        if (luState.isSecondParameter()) {
                            putBack(c)
                            luState.startLambda()
                            Token.lambda(position)
                        } else {
                            luState.addParameter()
                            Token.`var`(c.toChar().toString(), position)
                        }
                    }
                    in 'A'..'Z' -> {
                        if (luState.isSecondParameter()) {
                            putBack(c)
                            luState.startLambda()
                            Token.lambda(position)
                        } else {
                            var t = c
                            val name = StringBuilder()
                            while (t.toChar() in 'A'..'Z') {
                                name.append(t.toChar())
                                t = readNext()
                            }
                            putBack(t)
                            luState.addParameter()
                            Token.`var`(name.toString(), position)
                        }
                    }
                    else -> throw LexerException("Unexpected token '${c.toChar()}'", position)
                }
            )
            c = readSkippingWhitespace()
        }
        tokens.add(Token.eof)
        return tokens.asSequence()
    }

    /**
     * Reads next character from the input reader
     *
     * Skips whitespace and converts backslash '\' into λ
     */
    private fun readSkippingWhitespace(): Int {
        var c = readNext()
        while (whitespace.contains(c)) {
            c = readNext()
        }
        return if (c.toChar() == '\\') {
            'λ'.toInt()
        } else {
            c
        }
    }

    private fun readNext(): Int = if (buffer.isNotEmpty()) {
        buffer.pop()
    } else {
        position += 1
        reader.read()
    }

    private fun putBack(c: Int) = buffer.push(c)

    companion object {
        private val whitespace = setOf(' ', '\t', '\n', '\r').map { it.toInt() }
    }
}