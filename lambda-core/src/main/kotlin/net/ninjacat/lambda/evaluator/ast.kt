package net.ninjacat.lambda.evaluator


/**
 * Basic term of lambda grammar
 */
sealed class Term {
    /**
     * Proper lambda-calculus representation of the term
     */
    open fun repr() = toString()

    /**
     * Representation of the term with De Bruijn indexes
     */
    open fun indexedRepr() = toString()

    internal abstract fun shift(by: Int, from: Int = 0): Term
    internal abstract fun substitute(with: Term, depth: Int = -1): Term

    /**
     * Tries to resolve unbound variables by replacing them with named terms from passed context
     */
    abstract fun resolve(context: Map<String, Term>): Term
}

/**
 * Variable AKA Identifier. Contains variable name and it's De Bruijn index
 */
data class Variable(val name: String, val bindingIndex: Int) : Term() {
    override fun toString(): String = "$name[$bindingIndex]"

    override fun repr(): String = name

    override fun indexedRepr(): String = "${if (bindingIndex >= 0) bindingIndex.toString() else name} "

    override fun shift(by: Int, from: Int): Variable =
        Variable(name, bindingIndex + if (bindingIndex >= from) by else 0)

    override fun substitute(with: Term, depth: Int): Term {
        return if (depth == bindingIndex) {
            with.shift(depth)
        } else {
            this
        }
    }

    override fun resolve(context: Map<String, Term>): Term =
        if (bindingIndex < 0 && context.containsKey(name)) {
            context.getValue(name)
        } else {
            this
        }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Variable

        if (bindingIndex != other.bindingIndex) return false

        return true
    }

    override fun hashCode(): Int {
        return bindingIndex
    }


    companion object {
        fun parameter(name: String) = Variable(name, -1)
    }
}

/**
 * Assignment is not a part of lambda calculus, but allows binding of any term to a name
 * Only expressions not containing free variables can be assigned to
 */
data class Assignment(val variable: Variable, val value: Term) : Term() {
    override fun resolve(context: Map<String, Term>): Term = this

    override fun shift(by: Int, from: Int): Term = this
    override fun substitute(with: Term, depth: Int): Term = this

    override fun toString(): String = "$variable := $value".trim()
    override fun repr(): String = "${variable.repr()} := ${value.repr()}".trim()
}

/**
 * Lambda abstraction
 */
data class Abstraction(private val param: Variable, internal val body: Term) : Term() {
    private val strRepr = lazy {
        "λ$param.$body"
    }

    private val repr = lazy {
        "λ${param.repr()}.${body.repr()}"
    }

    override fun toString(): String = strRepr.value

    override fun repr(): String = repr.value
    override fun indexedRepr(): String = "λ ${body.indexedRepr()}".trim()
    override fun shift(by: Int, from: Int): Abstraction = Abstraction.of(param).`as`(body.shift(by, from + 1))

    override fun substitute(with: Term, depth: Int): Term = Abstraction.of(param).`as`(body.substitute(with, depth + 1))

    override fun resolve(context: Map<String, Term>): Term = Abstraction.of(param).`as`(body.resolve(context))


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

/**
 * Application
 */
data class Application(val a: Term, val b: Term) : Term() {
    private val toDeBruijnStrRepr = lazy {
        if (a is Abstraction) "(${a.indexedRepr()})(${b.indexedRepr()})" else "${a.indexedRepr()}${b.indexedRepr()}"
    }

    private val toStrRepr = lazy {
        if (a is Abstraction) "($a)($b)" else "$a$b"
    }
    private val repr = lazy {
        if (a is Abstraction) "(${a.repr()})(${b.repr()})" else "${a.repr()}${b.repr()}"
    }
    override fun shift(by: Int, from: Int): Application = Application(
        a.shift(by, from),
        b.shift(by, from)
    )

    override fun substitute(with: Term, depth: Int): Term = Application(
        a.substitute(with, depth),
        b.substitute(with, depth)
    )

    override fun resolve(context: Map<String, Term>): Term = Application(
        a.resolve(context), b.resolve(context)
    )

    override fun repr(): String = repr.value
    override fun toString(): String = toStrRepr.value
    override fun indexedRepr(): String = toDeBruijnStrRepr.value

    companion object {
        fun of(vararg terms: Term) = terms.reduce { lhs, rhs -> Application(lhs, rhs) }
    }
}

