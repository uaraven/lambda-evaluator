package net.ninjacat.lambda.parser

import net.ninjacat.lambda.evaluator.*
import java.io.StringReader
import java.util.*


typealias BindingContext = List<String>

/**
 * λ calculus parser.
 */
class Parser(tokens: Sequence<Token>) {

    private val reader = tokens.iterator()
    private val buffer: Deque<Token> = LinkedList<Token>()

    fun parse(): Term = parseTerm(this::nonEof, listOf())

    /**
     * term ::= assignment | application | LAMBDA var+ DOT term
     */
    private fun parseTerm(shouldAccept: (Token) -> Boolean, context: BindingContext): Term {
        val t = readNext()
        return if (t.type == TokenType.LAMBDA) {
            parseLambda(shouldAccept, context)
        } else {
            val next = readNext()
            putBack(next)
            putBack(t)
            when {
                next.type == TokenType.ASSIGN -> parseAssignment(context)
                else -> parseApplication(shouldAccept, context)
            }
        }
    }

    /**
     * Parses lambda, calculating De Bruijn indices for bound variables
     */
    private fun parseLambda(shouldAccept: (Token) -> Boolean, context: BindingContext): Abstraction {
        val t = readNext()
        if (!shouldAccept(t) || t.type != TokenType.VARIABLE) {
            throw ParsingException("Identifier expected, but '$t' found")
        }
        val id = Variable.parameter(t.value)
        val dot = readNext()
        if (!shouldAccept(dot) || dot.type != TokenType.DOT) {
            throw ParsingException("'.' expected, but '$t' found")
        }
        val body = parseTerm(shouldAccept, listOf(id.name) + context)
        return Abstraction.of(id).`as`(body)
    }

    /**
     * application ::= atom | application'
     * application' ::= atom application' | empty
     */
    private fun parseApplication(shouldAccept: (Token) -> Boolean, context: BindingContext): Term {
        var lhs = parseAtom(context)
        while (true) {
            val nextToken = readNext()
            if (!shouldAccept(nextToken)) {
                return lhs
            }
            putBack(nextToken)
            val rhs = parseAtom(context)
            lhs = Application(lhs, rhs)
        }
    }

    /**
     * Parses assignment expression
     */
    private fun parseAssignment(context: BindingContext): Term {
        val lhs = readNext()
        if (lhs.type != TokenType.VARIABLE) {
            throw ParsingException("Expected variable name, but found $lhs")
        }
        val assign = readNext()
        if (assign.type != TokenType.ASSIGN) {
            throw ParsingException("Expected ':=', but found $assign")
        }
        val rhs = parseTerm(this::nonEof, context)
        return Assignment(Variable.parameter(lhs.value), rhs)
    }

    // atom ::= LPAREN term RPAREN | var
    private fun parseAtom(context: BindingContext): Term {
        val token = readNext()
        return when {
            token.type == TokenType.OPEN_PARENS -> parseTerm(this::nonClose, context)
            token.type == TokenType.VARIABLE -> Variable(token.value, context.indexOf(token.value))
            else -> throw ParsingException("Expected variable or '(', but found $token")
        }
    }

    private fun nonEof(t: Token) = t.type != TokenType.EOF
    private fun nonClose(t: Token) = t.type != TokenType.CLOSE_PARENS

    private fun readNext(): Token = if (buffer.isNotEmpty()) buffer.pop() else reader.next()

    private fun putBack(c: Token) = buffer.push(c)

    companion object {
        fun parse(str: String) = Parser(Lexer(StringReader(str)).tokenize()).parse()
    }
}