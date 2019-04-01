package net.ninjacat.lambda.parser

class ParsingException(message: String): Exception(message)

data class LexerException(override val message: String, val position: Int):
    Exception("Lexer error at position $position: $message")