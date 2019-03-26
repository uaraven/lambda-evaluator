package net.ninjacat.lambda.parser

import java.io.Reader
import java.util.*

/**
 * λ calculus parser.
 * <pre>
 * Splits input into
 *  - Groups (everything in brackets)
 *  - Lambdas
 *    - Variables
 * </pre>
 */
class Parser(private val reader: Reader) {

    private val buffer: Deque<Int> = LinkedList<Int>()

    fun tokenize(): Term {
        var c = readSkippingWhitespace()
        val terms = mutableListOf<Term>()
        while (c != -1) {
            val term = when (c.toChar()) {
                '(' -> readTerm().simplify()
                '\\' -> readLambda().simplify()
                'λ' -> readLambda().simplify()
                else -> throw ParsingException("Expected '(', '\\' or 'λ' but found '${c.toChar()}'")
            }
            terms.add(term)
            c = readSkippingWhitespace()
        }
        return Group(terms).simplify()
    }

    private fun readSkippingWhitespace(): Int {
        var c = readNext()
        while (whitespace.contains(c)) {
            c = readNext()
        }
        return c
    }

    private fun readTerm(): Term {
        val blockTerms = mutableListOf<Term>()
        var c = readSkippingWhitespace()
        termLoop@while (!isDelimiter(c)) {
            when (c) {
                '('.toInt() -> blockTerms.add(readTerm().simplify())
                '\\'.toInt(),
                'λ'.toInt() -> blockTerms.add(readLambda().simplify())
                ')'.toInt() -> break@termLoop
                -1 -> throw ParsingException("Expected ')' but found EOF")
                else ->
                    if (!whitespace.contains(c)) {
                        blockTerms.add(Variable(c.toChar()))
                    }
            }
            c = readSkippingWhitespace()
        }
        return Group(blockTerms.toList()).simplify()
    }

    private fun readLambda(): Term {
        val params = readParams()
        val body = readLambdaBody()
        return Lambda(params, body)
    }

    private fun readLambdaBody(): List<Term> {
        val bodyTerms = mutableListOf<Term>()
        var c = readSkippingWhitespace()
        while (!isDelimiter(c)) {
            when (c) {
                '('.toInt() -> bodyTerms.add(readTerm().simplify())
                '\\'.toInt(),
                'λ'.toInt() -> bodyTerms.add(readLambda().simplify())
                else ->
                    if (!whitespace.contains(c)) {
                        bodyTerms.add(Variable(c.toChar()))
                    }
            }
            c = readSkippingWhitespace()
        }
        return bodyTerms.toList()
    }

    private fun isDelimiter(c: Int): Boolean = lambdaBodyDelimiter.contains(c)

    private fun readParams(): List<Variable> {
        val params = mutableListOf<Variable>()
        var c = readSkippingWhitespace()
        while (c != '.'.toInt() && c != -1) {
            while (whitespace.contains(c)) {
                c = readNext()
            }
            if (c.toChar() in 'a'..'z') {
                params.add(Variable(c.toChar()))
            } else {
                throw ParsingException("Expected lowercase variable name, but found '${c.toChar()}'")
            }
            c = readSkippingWhitespace()
        }
        return params.toList()
    }

    private fun readNext(): Int = if (buffer.isNotEmpty()) buffer.pop() else reader.read()

    private fun putBack(c: Int) = buffer.push(c)

    companion object {
        private val whitespace = setOf(' ', '\t').map { it.toInt() }
        private val lambdaBodyDelimiter = setOf(')'.toInt(), '\n'.toInt(), -1)
    }
}