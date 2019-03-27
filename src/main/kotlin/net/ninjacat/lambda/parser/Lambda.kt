package net.ninjacat.lambda.parser

sealed class Term {
    open fun simplify(): Term = this
}

data class Variable(private val name: Char) : Term() {
    override fun toString(): String = name.toString()
}


data class Lambda(private val params: List<Variable>, private val body: List<Term>) : Term() {
    private val repr = lazy {
        "λ${params.joinToString("")}.${body.joinToString("")}"
    }

    override fun toString(): String = repr.value

    /**
     * Curries the lambda with multiple parameters,
     * i.e. λxy.xayb -> λx.λy.xayb
     */
    override fun simplify(): Lambda {
        return if (params.size > 1) {
            val reversed = params.reversed()
            reversed
                .drop(1)
                .fold(Lambda.of(reversed.first()).`as`(body)) { acc, `var` ->
                    Lambda.of(`var`).`as`(acc)
                }
        } else {
            this
        }
    }

    companion object {
        data class LambdaBuilder(val params: List<Variable>) {
            fun `as`(vararg body: Term) =
                Lambda(params, body.toList())

            fun `as`(body: List<Term>) = Lambda(params, body)
        }

        fun of(vararg params: Variable) =
            LambdaBuilder(params.toList())
    }
}

data class Group(private val terms: List<Term>) : Term() {
    private val repr = lazy { "(${terms.joinToString("") { "$it" }})" }

    override fun toString(): String = repr.value

    // If group contains only one parameter, unwraps it
    override fun simplify(): Term = if (terms.size == 1) terms.first() else this

    companion object {
        fun of(vararg term: Term) = Group(term.toList())
    }

    /**
     * Returns true if this group contains lambda application
     */
    fun isApplication() = terms.first() is Lambda
}

