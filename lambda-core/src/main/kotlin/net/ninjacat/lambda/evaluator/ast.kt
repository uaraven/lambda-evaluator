package net.ninjacat.lambda.evaluator


sealed class Term {
    open fun simplify(): Term = this
    open fun alphaConversion(): Term {
        val renaming = boundVariables().withIndex().map { Pair(it.value.name, "x'${it.index}'") }.toMap()
        return substitute(renaming)
    }

    open fun betaReduction(context: Context): Term = this
    open fun etaReduction(context: Context): Term = this

    internal abstract fun substitute(nameMapping: Map<String, String>): Term

    abstract fun freeVariables(): Sequence<Variable>
    abstract fun boundVariables(): Sequence<Variable>
}

data class Variable(val name: String) : Term() {
    override fun toString(): String = name

    override fun freeVariables(): Sequence<Variable> = setOf(this).asSequence()
    override fun boundVariables(): Sequence<Variable> = setOf<Variable>().asSequence()

    override fun substitute(nameMapping: Map<String, String>): Variable =
        if (nameMapping.containsKey(name)) {
            Variable(nameMapping.getValue(name))
        } else {
            this
        }
}

/**
 * Assignment is not a part of lambda calculus, but allows binding of any term to a name
 * Only expressions not containing free variables can be assigned to
 */
data class Assignment(val variable: Variable, val value: Term) : Term() {
    override fun freeVariables(): Sequence<Variable> = value.freeVariables()
    override fun boundVariables(): Sequence<Variable> = value.boundVariables()

    override fun substitute(nameMapping: Map<String, String>): Term =
        Assignment(variable, value.substitute(nameMapping))
}

/**
 * Lambda abstraction
 */
data class Abstraction(private val param: Variable, private val body: Term) : Term() {
    private val repr = lazy {
        "λ$param.$body"
    }

    override fun toString(): String = repr.value

    override fun freeVariables() = body.freeVariables().filterNot { it.name == param.name }
    override fun boundVariables(): Sequence<Variable> {
        val freeVarNames = freeVariables().map { it.name }.toSet()
        return setOf(param).filterNot { freeVarNames.contains(it.name) }.asSequence()
    }

    override fun substitute(nameMapping: Map<String, String>): Abstraction =
        Abstraction(param.substitute(nameMapping), body.substitute(nameMapping))

    override fun simplify(): Abstraction = this

    companion object {
        data class LambdaBuilder(val params: List<Variable>) {
            fun `as`(body: Term): Abstraction {
                val reversed = params.reversed()
                return reversed
                    .drop(1)
                    .fold(Abstraction(reversed.first(), body)) { acc, `var` ->
                        Abstraction(`var`, acc)
                    }
            }
        }

        fun of(params: List<Variable>) = LambdaBuilder(params)

        fun of(vararg params: String) =
            LambdaBuilder(params.map { Variable(it) }.toList())

        fun of(vararg params: Variable) =
            LambdaBuilder(params.toList())

    }
}

data class Application(val a: Term, val b: Term) : Term() {
    private val repr = lazy {
        if (a is Abstraction) "($a)(b)" else "$a$b"
    }

    override fun toString(): String = repr.value

    override fun freeVariables(): Sequence<Variable> {
        return (a.freeVariables().toSet() + b.freeVariables().toSet()).asSequence()
    }

    override fun boundVariables(): Sequence<Variable> {
        return (a.boundVariables().toSet() + b.boundVariables().toSet()).asSequence()
    }

    override fun substitute(nameMapping: Map<String, String>): Application =
        Application(a.substitute(nameMapping), b.substitute(nameMapping))

    companion object {
        fun of(vararg terms: Term) = terms.reduce { lhs, rhs -> Application(lhs, rhs) }
    }
}

