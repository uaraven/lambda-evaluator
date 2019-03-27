package net.ninjacat.lambda.parser


class Context

sealed class Term {
    open fun simplify(): Term = this
    open fun evaluate(context: Context): Term = this
}


data class Variable(private val name: String) : Term() {
    override fun toString(): String = name
}

data class Assignment(val variable: Variable, val value: Term) : Term()


data class Lambda(private val params: List<Variable>, private val body: Term) : Term() {
    private val repr = lazy {
        "λ${params.joinToString("")}.$body"
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
                Lambda(params, Group.of(body.toList()).simplify())

            fun `as`(body: List<Term>) = Lambda(params, Group.of(body).simplify())
        }

        fun of(vararg params: Variable) =
            LambdaBuilder(params.toList())
    }
}

data class Application(val func: Term, val params: Term) : Term() {
    override fun toString(): String = "$func$params"
}

data class Group(private val terms: List<Term>) : Term() {
    private val repr = lazy {
        "(${terms.joinToString("") { "$it" }})"
    }

    override fun toString(): String = repr.value

    // If group contains only one parameter, unwraps it
    override fun simplify(): Term = if (terms.size == 1) terms.first() else this

    companion object {
        fun of(vararg term: Term) = Group(term.toList())
        fun of(vars: List<Term>) = Group(vars)
    }

    /**
     * Returns true if this group contains lambda application
     */
    fun isApplication() = terms.first() is Lambda
}

