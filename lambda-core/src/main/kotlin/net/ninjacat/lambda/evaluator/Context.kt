package net.ninjacat.lambda.evaluator

sealed class Context(private val parent: Context?) {
    private val boundVars = mutableMapOf<String, Term>()
    fun getValue(name: String): Term? = boundVars[name] ?: parent?.getValue(name)
    fun putValue(name: String, term: Term) { boundVars[name] = term }
}

class RootContext(): Context(null)