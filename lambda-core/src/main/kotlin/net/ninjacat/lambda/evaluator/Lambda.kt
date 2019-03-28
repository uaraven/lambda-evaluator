package net.ninjacat.lambda.evaluator


sealed class Term {
    open fun simplify(): Term = this
    open fun alphaConversion(): Term = this
    open fun betaReduction(context: Context): Term = this
    open fun etaReduction(context: Context): Term = this

    open fun listVariables() = sequenceOf<Variable>()
}


data class Variable(val name: String) : Term() {
    override fun toString(): String = name
}

data class Assignment(val variable: Variable, val value: Term) : Term()


data class Lambda(private val params: List<Variable>, private val body: Term) : Term() {
    private val paramSet = params.map { it.name }.toSet()

    private val repr = lazy {
        "λ${params.joinToString("")}.$body"
    }

    override fun toString(): String = repr.value

    fun freeVariables() = body.listVariables()
        .filterNot { paramSet.contains(it.name) }

    fun boundVariables() = body.listVariables()
        .filter { paramSet.contains(it.name) }

//    override fun alphaConversion(): Term {
//        val listToChange = boundVariables()
//
//    }

    /**
     * Curries the lambda with multiple parameters,
     * i.e. λxy.xayb -> λx.λy.xayb
     */
    override fun simplify(): Lambda {
        return if (params.size > 1) {
            val reversed = params.reversed()
            reversed
                .drop(1)
                .fold(of(reversed.first()).`as`(body)) { acc, `var` ->
                    of(`var`).`as`(acc)
                }
        } else {
            this
        }
    }

    companion object {
        data class LambdaBuilder(val params: List<Variable>) {
            fun `as`(body: Term) = Lambda(
                params,
                body.simplify()
            )
        }

        fun of(vararg params: Variable) =
            LambdaBuilder(params.toList())
    }
}

data class Application(val a: Term, val b: Term) : Term() {
    val repr = lazy {
        if (a is Lambda) "($a)(b)" else "$a$b"
    }
    override fun toString(): String = repr.value

    companion object {
        fun of(vararg terms: Term) = terms.reduce { lhs, rhs -> Application(lhs, rhs) }
    }
}

data class Group(private val terms: List<Term>) : Term() {
    private val repr = lazy {
        "(${terms.joinToString("") { "$it" }})"
    }

    override fun listVariables(): Sequence<Variable> = terms.flatMap { it.listVariables().asIterable() }.toSet().asSequence()

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

