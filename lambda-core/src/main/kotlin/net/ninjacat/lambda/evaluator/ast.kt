package net.ninjacat.lambda.evaluator


sealed class Term {
    open fun repr() = toString()
    open fun toDeBruijnString() = toString()
}

data class Variable(val name: String, val bindingIndex: Int) : Term() {
    override fun toString(): String = "$name[$bindingIndex]"

    override fun repr(): String = name
    override fun toDeBruijnString(): String = "${if (bindingIndex >= 0) bindingIndex.toString() else name} "

    companion object {
        fun parameter(name: String) = Variable(name, -1)
    }
}

/**
 * Assignment is not a part of lambda calculus, but allows binding of any term to a name
 * Only expressions not containing free variables can be assigned to
 */
data class Assignment(val variable: Variable, val value: Term) : Term() {
    override fun toString(): String = "$variable := $value".trim()
    override fun repr(): String = "${variable.repr()} := ${value.repr()}".trim()
}

/**
 * Lambda abstraction
 */
data class Abstraction(private val param: Variable, private val body: Term) : Term() {
    private val strRepr = lazy {
        "λ$param.$body"
    }
    private val repr = lazy {
        "λ${param.repr()}.${body.repr()}"
    }
    override fun toString(): String = strRepr.value
    override fun repr(): String = repr.value
    override fun toDeBruijnString(): String ="λ ${body.toDeBruijnString()}".trim()

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

        fun of(vararg params: String) =
            LambdaBuilder(params.map { Variable.parameter(it) }.toList())

        fun of(vararg params: Variable) =
            LambdaBuilder(params.toList())

    }
}

data class Application(val a: Term, val b: Term) : Term() {
    private val toDeBruijnStrRepr = lazy {
        if (a is Abstraction) "(${a.toDeBruijnString()})(${b.toDeBruijnString()})" else "${a.toDeBruijnString()}${b.toDeBruijnString()}"
    }
    private val toStrRepr = lazy {
        if (a is Abstraction) "($a)($b)" else "$a$b"
    }
    private val repr = lazy {
        if (a is Abstraction) "(${a.repr()})(${b.repr()})" else "${a.repr()}${b.repr()}"
    }

    override fun repr(): String = repr.value
    override fun toString(): String = toStrRepr.value
    override fun toDeBruijnString(): String = toDeBruijnStrRepr.value

    companion object {
        fun of(vararg terms: Term) = terms.reduce { lhs, rhs -> Application(lhs, rhs) }
    }
}

