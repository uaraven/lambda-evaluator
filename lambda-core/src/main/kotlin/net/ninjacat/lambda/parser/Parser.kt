package net.ninjacat.lambda.parser

import net.ninjacat.lambda.evaluator.*
import java.io.StringReader
import java.util.*

/**
 * λ calculus parser.
 */
class Parser(tokens: Sequence<Token>) {

    private val reader = tokens.iterator()
    private val buffer: Deque<Token> = LinkedList<Token>()

    fun parse(): Term = parseTerm(this::nonEof)

    /**
     * term ::= assignment | application | LAMBDA var+ DOT term
     */
    private fun parseTerm(shouldAccept: (Token) -> Boolean): Term {
        var t = readNext()
        if (t.type == TokenType.LAMBDA) {
            t = readNext()
            val params = mutableListOf<Variable>()
            while (shouldAccept(t) && t.type != TokenType.DOT) {
                if (t.type == TokenType.VARIABLE) {
                    params.add(Variable(t.value))
                } else {
                    throw ParsingException("Expected variable name, but got $t")
                }
                t = readNext()
            }
            val body = parseTerm(this::lambdaBody).simplify()
            return Lambda(params, body).simplify()
        } else {
            val next = readNext()
            putBack(next)
            putBack(t)
            return when {
                next.type == TokenType.ASSIGN -> parseAssignment()
                else -> parseApplication(shouldAccept).simplify()
            }
        }
    }

    private fun parseVarGroup(): Term {
        val groupTerms = mutableListOf<Term>()
        var next = readNext()
        while (next.type == TokenType.VARIABLE) {
            groupTerms.add(Variable(next.value))
            next = readNext()
        }
        putBack(next)
        return Group.of(groupTerms)
    }

    /**
     * application ::= atom | application'
     * application' ::= atom application' | empty
     */
    private fun parseApplication(shouldAccept: (Token) -> Boolean): Term {
        val t1 = readNext()
        val t2 = readNext()
        putBack(t2)
        putBack(t1)
        var lhs = if (t1.type == t2.type && t1.type == TokenType.VARIABLE) {
            parseVarGroup()
        } else {
            parseAtom()
        }
        while (true) {
            val nextToken = readNext()
            if (!shouldAccept(nextToken)) {
                return lhs.simplify()
            }
            putBack(nextToken)
            val rhs = parseAtom()
            lhs = Application(lhs.simplify(), rhs.simplify())
        }
    }

    private fun parseAssignment(): Term {
        val lhs = readNext()
        if (lhs.type != TokenType.VARIABLE) {
            throw ParsingException("Expected variable name, but found $lhs")
        }
        val assign = readNext()
        if (assign.type != TokenType.ASSIGN) {
            throw ParsingException("Expected ':=', but found $assign")
        }
        val rhs = parseTerm(this::nonEof)
        return Assignment(Variable(lhs.value), rhs)
    }

    // atom ::= LPAREN term RPAREN | var+
    private fun parseAtom(): Term {
        val token = readNext()
        return when {
            token.type == TokenType.OPEN_PARENS -> parseTerm(this::nonClose).simplify()
            token.type == TokenType.VARIABLE -> Variable(token.value)
            else -> throw ParsingException("Expected variable or '(', but found $token")
        }
    }

    private fun nonEof(t: Token) = t.type != TokenType.EOF
    private fun nonClose(t: Token) = t.type != TokenType.CLOSE_PARENS
    private fun lambdaBody(t: Token): Boolean = t.type == TokenType.VARIABLE
            || t.type == TokenType.OPEN_PARENS
            || t.type == TokenType.LAMBDA

    private fun readNext(): Token = if (buffer.isNotEmpty()) buffer.pop() else reader.next()

    private fun putBack(c: Token) = buffer.push(c)

    companion object {
        fun parse(str: String) = Parser(Lexer(StringReader(str)).tokenize()).parse()
    }
}