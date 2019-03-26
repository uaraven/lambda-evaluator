package net.ninjacat.lambda.parser

import net.ninjacat.lambda.Group
import net.ninjacat.lambda.Lambda
import net.ninjacat.lambda.Term
import net.ninjacat.lambda.Variable
import java.io.Reader
import java.util.*

/**
 * Semblance Lexer. Splits input stream (represented as [Reader]) into tokens.
 *
 * No validation of tokens is performed
 */
class Tokenizer(private val reader: Reader) {

    private val buffer: Deque<Int> = LinkedList<Int>()

    fun tokenize(): Term {
        var c = readNext()
        while (whitespace.contains(c)) {
            c = readNext()
        }
        return when (c.toChar()) {
            '(' -> readTerm().simplify()
            '\\' -> readLambda().simplify()
            'λ' -> readLambda().simplify()
            else -> throw ParsingException("Expected '(', '\\' or 'λ' but found '${c.toChar()}")
        }
    }

    private fun readTerm(): Term {
        val blockTerms = mutableListOf<Term>()
        var c = readNext()
        while (whitespace.contains(c)) {
            c = readNext()
        }
        when (c) {
            '('.toInt() -> blockTerms.add(readTerm().simplify())
            '\\'.toInt(),
            'λ'.toInt() -> blockTerms.add(readLambda().simplify())
            ')'.toInt() -> return Group(blockTerms.toList()).simplify()
            -1 -> throw ParsingException("Expected ')' but found EOF")
            else ->
                if (!whitespace.contains(c)) {
                    blockTerms.add(Variable(c.toChar()))
                }
        }
        return Group(blockTerms.toList()).simplify()
    }

    private fun readLambda(): Term {
        val params = readParams()
        val body = readTerm().simplify()
        return Lambda(params, body)
    }

    private fun readParams(): List<Variable> {
        val params = mutableListOf<Variable>()
        var c = readNext()
        while (c != '.'.toInt() && c != -1) {
            while (whitespace.contains(c)) {
                c = readNext()
            }
            if (c.toChar() in 'a'..'z') {
                params.add(Variable(c.toChar()))
            } else {
                throw ParsingException("Expected lowercase variable name, but found '${c.toChar()}'")
            }
        }
        return params.toList()
    }

    private fun readNext(): Int = if (buffer.isNotEmpty()) buffer.pop() else reader.read()

    private fun putBack(c: Int) = buffer.push(c)

    companion object {
        private val whitespace = setOf(' ', '\t').map { it.toInt() }
    }
}