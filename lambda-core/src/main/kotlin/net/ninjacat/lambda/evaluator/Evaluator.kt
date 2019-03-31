package net.ninjacat.lambda.evaluator

/**
 * Evaluates lambda expression
 */
class Evaluator {

    private val context = mutableMapOf<String, Term>()

    fun eval(root: Term): List<Term> {
        val results = mutableListOf<Term>()
        System.out.println(root.repr() + " -> " + root.indexedRepr())
        root.resolve(context)
        internalEval(root, results)
        return results.toList()
    }

    private fun internalEval(term: Term, result: MutableList<Term>): Term {
        val evaluated = evaluationStep(term)
        System.out.println(evaluated.repr() + " -> " + evaluated.indexedRepr())
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
}