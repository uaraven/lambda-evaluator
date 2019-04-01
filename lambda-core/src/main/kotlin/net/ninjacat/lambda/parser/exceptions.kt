package net.ninjacat.lambda.parser

sealed class SyntaxException(message: String, val position: Int): Exception(message)

class ParsingException(message: String, position: Int): SyntaxException(message, position)

class LexerException(message: String, position: Int):
    SyntaxException("Lexer error at position $position: $message", position)