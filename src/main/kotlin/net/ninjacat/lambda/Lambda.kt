package net.ninjacat.lambda

sealed class Term() {
    open fun simplify(): Term = this
}

data class Variable(private val name: Char) : Term() {
    override fun toString(): String = name.toString()
}


data class Lambda(private val params: List<Variable>, private val body: Term) : Term() {
    private val lambda = "\uD835\uDF06"

    private val repr = lazy {
        "$lambda${params.joinToString("")}.${body}"
    }

    override fun toString(): String = repr.value

}

data class Group(private val terms: List<Term>) : Term() {
    private val repr = lazy {
        when {
            terms.all { it is Variable } -> "${terms.joinToString("") { "$it" }}"
            else -> "(${terms.joinToString("") { "$it" }})"
        }
    }

    override fun toString(): String = repr.value

    override fun simplify(): Term = if (terms.size == 1) terms.first() else this
}

