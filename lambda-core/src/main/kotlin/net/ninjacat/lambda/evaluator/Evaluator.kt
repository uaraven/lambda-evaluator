package net.ninjacat.lambda.evaluator

class Evaluator {

    fun eval(root: Term): List<Term> {
        val results = mutableListOf<Term>()
        internalEval(root, results)
        return results.toList()
    }

    private fun internalEval(term: Term, result: MutableList<Term>): Term {
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
                if (isValue(root.a) && isValue(root.b)) {
                    substitute(root.a, if (root.b is Abstraction) root.b.body else root.b)
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