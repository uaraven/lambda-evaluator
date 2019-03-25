package net.ninjacat.lambda

class Lambda(private val params: List<Char>, private val body: List<Char>) {

    private val repr = lazy {
        "\uD835\uDF06" + params.joinToString("") + "." + body.joinToString("")
    }

    override fun toString(): String = repr.value
}