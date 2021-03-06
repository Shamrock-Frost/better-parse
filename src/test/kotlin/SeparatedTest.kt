
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.token
import com.github.h0tk3y.betterParse.parser.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeparatedTest : Grammar<Nothing>() {
    override val rootParser: Parser<Nothing> get() = throw NoSuchElementException()

    val number by token("\\d+")
    val comma by token(",\\s+")
    val word by token("\\w+")

    @Test fun separate() {
        val tokens = tokenizer.tokenize("one, two, three")
        val result = separated(word use { text }, comma).tryParse(tokens).toParsedOrThrow().value

        assertEquals(listOf("one", "two", "three"), result.terms)
        assertEquals(2, result.separators.size)
    }

    @Test fun singleSeparated() {
        val tokens = tokenizer.tokenize("one")
        val result = separated(word use { text }, comma).parseToEnd(tokens)

        assertEquals(listOf("one"), result.terms)
        assertTrue(result.separators.isEmpty())
    }

    @Test fun acceptZero() {
        val tokens = tokenizer.tokenize("123")

        val resultRejectZero = separated(word asJust "x", comma).tryParse(tokens)
        assertTrue(resultRejectZero is MismatchedToken)

        val resultAcceptZero = separated(word asJust "x", comma, acceptZero = true).tryParse(tokens)
        assertTrue(resultAcceptZero is Parsed && resultAcceptZero.value.terms.isEmpty())
    }

    @Test fun reduceLeftRight() {
        val tokens = tokenizer.tokenize("3, 4, 5, 6")
        val result = separated(number use { text.toInt() }, comma).parseToEnd(tokens)

        val minusLeft = result.reduce { a, _, b -> a - b }
        assertEquals(3 - 4 - 5 - 6, minusLeft)

        val minusRight = result.reduceRight { x, _, y -> y - x }
        assertEquals(6 - 5 - 4 - 3, minusRight)
    }

    @Test fun associative() {
        val tokens = tokenizer.tokenize("3, 4, 5, 6")
        val p = (number use { text.toInt() }) as Parser<Any>

        val resultLeft = leftAssociative(p, comma) { a, _, b -> a to b }.parseToEnd(tokens)
        assertEquals(((3 to 4) to 5) to 6, resultLeft)

        val resultRight = rightAssociative(p, comma) { a, _, b -> a to b }.parseToEnd(tokens)
        assertEquals(3 to (4 to (5 to 6)), resultRight)
    }
}