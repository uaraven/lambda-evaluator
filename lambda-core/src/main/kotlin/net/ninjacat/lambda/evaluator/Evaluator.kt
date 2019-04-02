package net.ninjacat.lambda.evaluator

/**
 * Evaluates lambda expression
 */
class Evaluator {

    private val context = mutableMapOf<String, Term>()

    fun eval(root: Term): List<Term> {
        val results = mutableListOf<Term>()
        val term= root.resolve(context)
        if (term is Assignment) {
            val lastEvaluated = internalEval(term.value, results)
            if (results.isEmpty()) {
                results.add(lastEvaluated)
            }
            context[term.variable.name] = lastEvaluated
        } else {
            results.add(term)
            internalEval(term, results)
        }
        return results.toList()
    }

    fun getNamed(name: String): Term? = context[name]

    private fun internalEval(term: Term, result: MutableList<Term>): Term {
        if (result.size > recursionLimit) {
            return result.last()
        }
        val evaluated = evaluationStep(term)
        return if (evaluated == term) {
            evaluated
        } else {
            result.add(evaluated)
            internalEval(evaluated, result)
        }
    }

    private fun isValue(term: Term) = term is Abstraction || term is Variable

    private fun substitute(where: Term, with: Term): Term = where.substitute(with.shift(1)).shift(-1)

    private fun evaluationStep(root: Term): Term {
        return when (root) {
            is Application -> {
                if (root.a is Variable && root.b is Variable) {
                    root
                } else if (isValue(root.a) && isValue(root.b)) {
                    val newAbstraction = substitute(root.a, root.b)
                    (newAbstraction as Abstraction).body
                } else if (isValue(root.a)) {
                    Application.of(root.a, evaluationStep(root.b))
                } else {
                    Application.of(evaluationStep(root.a), root.b)
                }
            }
            else -> root
        }
    }

    companion object {
        const val recursionLimit = 15
    }
}