package net.ninjacat.lambda.parser


/**
 * Enumeration of supported token types.
 */
enum class TokenType {
    Lambda,
    Variable,
    OpenParens,
    CloseParens,
    Dot,
    Eof;
}

/**
 * Lexical token
 */
data class Token(val value: String, val type: TokenType) {

    fun repr() = "$type: '$value'"

    companion object {

        fun eof() = Token("", TokenType.Eof)
        fun lambda() = Token("λ", TokenType.Lambda)
        fun variable(name: Char) = Token(name.toString(), TokenType.Variable)
        fun dot() = Token(".", TokenType.Dot)

        fun openParens() = Token("(", TokenType.OpenParens)
        fun closeParens() = Token(")", TokenType.CloseParens)
    }
}