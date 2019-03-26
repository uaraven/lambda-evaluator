package net.ninjacat.lambda.parser

import net.ninjacat.lambda.parser.Token.Companion.dot
import net.ninjacat.lambda.parser.Token.Companion.lambda
import net.ninjacat.lambda.parser.Token.Companion.variable
import org.hamcrest.Matchers.equalTo
import org.junit.Test

import org.junit.Assert.*
import java.io.StringReader

class TokenizerTest {

    @Test
    fun testSimpleLambda() {
        val reader = StringReader("\\xy.xay")
        val tokenizer = Tokenizer(reader)

    }

    @Test
    fun testGroupedLambda() {
        val reader = StringReader("((\\xy.xay)(a b))")
        val tokenizer = Tokenizer(reader)

    }
}