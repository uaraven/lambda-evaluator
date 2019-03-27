package net.ninjacat.lambda.parser

import java.io.Reader
import java.util.*

/**
 * λ calculus parser.
 */
class Parser(private val reader: Reader) {

    private val buffer: Deque<Int> = LinkedList<Int>()

    fun parse(): Term = parseTerm()

    /**
     * term ::= assignment | application | LAMBDA var+ DOT term
     */
    private fun parseTerm(final: Int = -1): Term {
        var t = readSkippingWhitespace()
        if (isLambda(t)) {
            t = readSkippingWhitespace()
            val params = mutableListOf<Variable>()
            while (t != final && t != '.'.toInt()) {
                if (t.toChar() in 'a'..'z') {
                    params.add(Variable(t.toChar()))
                } else {
                    throw ParsingException("Expected variable name, but got '${t.toChar()}'")
                }
                t = readSkippingWhitespace()
            }
            val body = parseTerm(-1)
            return Lambda(params, body).simplify()
        } else {
            val next = readSkippingWhitespace()
            putBack(next)
            if (next.toChar() == '=') {
                putBack(t)
                return parseAssignment()
            } else {
                putBack(t)
                return parseApplication(final)
            }
        }
    }

    /**
     * application ::= atom | application'
     * application' ::= atom application' | empty
     */
    private fun parseApplication(final: Int = -1): Term {
        var lhs = parseAtom()
        while (true) {
            val nextToken = readSkippingWhitespace()
            if (nextToken == final) {
                return lhs
            }
            putBack(nextToken)
            val rhs = parseAtom()
            lhs = Application(lhs, rhs)
        }
    }

    private fun parseAssignment(): Term {
        val lhs = readSkippingWhitespace()
        val assign = readSkippingWhitespace()
        if (assign.toChar() != '=') {
            throw ParsingException("Expected '=', but found '${assign.toChar()}'")
        }
        val rhs = parseTerm()
        return Assignment(Variable(lhs.toChar()), rhs)
    }

    // atom ::= LPAREN term RPAREN | var+
    private fun parseAtom(): Term {
        var token = readSkippingWhitespace()
        if (token == '('.toInt()) {
            return parseTerm(')'.toInt())
        } else {
            val vars = mutableListOf<Variable>()
            while (token.toChar() in 'a'..'z') {
                vars.add(Variable(token.toChar()))
                token = readSkippingWhitespace()
            }
            putBack(token)
            return Group.of(vars.toList()).simplify()
        }
    }

    private fun readSkippingWhitespace(): Int {
        var c = readNext()
        while (whitespace.contains(c)) {
            c = readNext()
        }
        return c
    }

    private fun isLambda(c: Int) = c == '\\'.toInt() || c == 'λ'.toInt()

    private fun isDelimiter(c: Int): Boolean = lambdaBodyDelimiter.contains(c)

    private fun isNotDelimiter(c: Int): Boolean = !isDelimiter(c)

    private fun readNext(): Int = if (buffer.isNotEmpty()) buffer.pop() else reader.read()

    private fun putBack(c: Int) = buffer.push(c)

    companion object {
        private val whitespace = setOf(' ', '\t').map { it.toInt() }
        private val lambdaBodyDelimiter = setOf(')'.toInt(), '\n'.toInt(), -1)
    }
}