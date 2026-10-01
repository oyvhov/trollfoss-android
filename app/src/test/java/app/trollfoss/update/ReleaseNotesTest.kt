package app.trollfoss.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseNotesTest {

    // The start of docs/release-v1.0.0.md, as GitHub sends it (CRLF line endings).
    private val v100 = listOf(
        "![Trollfoss](https://raw.githubusercontent.com/oyvhov/trollfoss-android/main/docs/images/banner.png)",
        "",
        "## Trollfoss 1.0.0 – første utgåve",
        "",
        "Eit digitalt dukkehus for barn frå 4 til 10 år, for Android-mobil og -nettbrett (liggjande). Ei heil bygd",
        "under ein stor foss, med folk, dyr og troll å leike med. Ingen reglar, ingen poeng og ingenting å tape.",
        "",
        "### Kva er med",
        "",
        "- **15 stader** i skrå-3D: Heime, Bakeriet, Frisøren, Stranda, Fossen, Trollhola, Fjellet, Garden,",
        "  Romstasjonen, Tivoliet, Butikken, Legekontoret, Scena, Havbotnen og **Heileberget** – eit stort, langt",
        "  fjell med taubane, ekko-stein, ørnerede, toppflagg og fjellgeiter.",
        "- **Oppdragstavle** med tre biletoppdrag om gongen, klistremerkealbum og spesialmøblar som låsast opp.",
        "- **Figurverkstad** med hud, høgd, frisyre, klede og namn.",
        "",
        "### Installere",
        "",
        "Last ned `Trollfoss-v1.0.0.apk` under, opne fila på eininga og vel **Installer**.",
    ).joinToString("\r\n")

    @Test
    fun `the first release reads as plain text`() {
        val plain = markdownToPlain(v100)
        assertEquals(
            listOf(
                "Trollfoss 1.0.0 – første utgåve",
                "",
                "Eit digitalt dukkehus for barn frå 4 til 10 år, for Android-mobil og -nettbrett (liggjande). " +
                    "Ei heil bygd under ein stor foss, med folk, dyr og troll å leike med. Ingen reglar, ingen poeng og ingenting å tape.",
                "",
                "Kva er med",
                "",
                "• 15 stader i skrå-3D: Heime, Bakeriet, Frisøren, Stranda, Fossen, Trollhola, Fjellet, Garden, Romstasjonen, " +
                    "Tivoliet, Butikken, Legekontoret, Scena, Havbotnen og Heileberget – eit stort, langt fjell med taubane, " +
                    "ekko-stein, ørnerede, toppflagg og fjellgeiter.",
                "• Oppdragstavle med tre biletoppdrag om gongen, klistremerkealbum og spesialmøblar som låsast opp.",
                "• Figurverkstad med hud, høgd, frisyre, klede og namn.",
                "",
                "Installere",
                "",
                "Last ned Trollfoss-v1.0.0.apk under, opne fila på eininga og vel Installer.",
            ).joinToString("\n"),
            plain,
        )
    }

    @Test
    fun `the panel text is short, plain and cut between words`() {
        val notes = plainReleaseNotes(v100)
        assertTrue(notes.length <= 360)
        assertTrue(notes.lines().size <= 10)
        assertTrue(notes.startsWith("Trollfoss 1.0.0 – første utgåve\n\nEit digitalt dukkehus"))
        assertTrue(notes.endsWith("…"))
        for (mark in listOf("![", "](", "#", "**", "`", "<", "\r")) assertFalse(mark, notes.contains(mark))
        // The word before the ellipsis is a whole word from the source.
        val lastWord = notes.removeSuffix("…").substringAfterLast(' ')
        assertTrue(lastWord, Regex("(^|\\s)${Regex.escape(lastWord)}[\\s,.:;]").containsMatchIn(markdownToPlain(v100)))
    }

    @Test
    fun `html, comments, images and rules disappear`() {
        val body = """
            <!-- written by hand
            over two lines -->
            <p align="center"><img src="https://example.com/logo.png" width="200"></p>

            [![badge](https://example.com/b.svg)](https://example.com)
            Nytt: <b>fossen</b> syng<br>no.

            ---
            Sjå [endringsloggen](https://example.com/log) for meir. 4 < 10 og 10 > 4.
        """.trimIndent()
        assertEquals("Nytt: fossen syngno.\n\nSjå endringsloggen for meir. 4 < 10 og 10 > 4.", markdownToPlain(body))
    }

    @Test
    fun `headings keep their words and lists get bullets`() {
        val body = "# Stort ##\n## Lite\n* stjerne\n+ pluss\n1. først\n2) andre\n-ikkje liste\n#emneknagg"
        assertEquals("Stort\nLite\n• stjerne\n• pluss\n1. først\n2) andre -ikkje liste #emneknagg", markdownToPlain(body))
    }

    @Test
    fun `runs of blank lines collapse and nothing is left at the edges`() {
        assertEquals("Ein\n\nTo", markdownToPlain("\n\n   \nEin\n\n\n\n \t\nTo\n\n\n"))
        assertEquals("", markdownToPlain("![bare eit bilete](https://example.com/a.png)\n\n<br>\n"))
        assertEquals("", plainReleaseNotes(""))
    }

    @Test
    fun `short notes are left alone`() {
        assertEquals("• Fiks for kameraet.", plainReleaseNotes("- Fiks for **kameraet**."))
    }

    @Test
    fun `a cut between words drops trailing punctuation and adds an ellipsis`() {
        assertEquals("Ein to tre…", shortenNotes("Ein to tre, fire fem seks", maxChars = 13, maxLines = 5))
        assertEquals("Eitt…", shortenNotes("Eitt langt ord", maxChars = 10, maxLines = 5))
    }

    @Test
    fun `a line break near the limit wins and the ellipsis gets its own line`() {
        val text = "Første linje er lang nok.\nAndre linje held fram lenge etter grensa."
        assertEquals("Første linje er lang nok.\n…", shortenNotes(text, maxChars = 32, maxLines = 5))
    }

    @Test
    fun `a far line break gives way to a cut between words`() {
        val text = "Kort.\nDenne linja er mykje lengre enn grensa tillèt."
        assertEquals("Kort.\nDenne linja er mykje…", shortenNotes(text, maxChars = 30, maxLines = 5))
    }

    @Test
    fun `too many lines are cut at a line`() {
        val text = (1..20).joinToString("\n") { "• Punkt $it" }
        val cut = shortenNotes(text, maxChars = 1000, maxLines = 4)
        assertEquals("• Punkt 1\n• Punkt 2\n• Punkt 3\n…", cut)
        assertEquals("Ein\n…", shortenNotes("Ein\n\nTo\nTre", maxChars = 100, maxLines = 3))
    }

    @Test
    fun `one endless word is still cut to size`() {
        val cut = shortenNotes("a".repeat(100), maxChars = 20, maxLines = 5)
        assertEquals("a".repeat(19) + "…", cut)
    }

    @Test
    fun `limits always hold`() {
        val words = listOf("fossen", "troll", "Heileberget", "-", "**", "\n", "\n\n", "![x](y)", "<i>", "## ", "`kode`")
        val random = java.util.Random(7)
        repeat(500) {
            val body = (0 until random.nextInt(200)).joinToString(" ") { words[random.nextInt(words.size)] }
            val maxChars = 2 + random.nextInt(120)
            val maxLines = 2 + random.nextInt(8)
            val notes = plainReleaseNotes(body, maxChars, maxLines)
            assertTrue(notes, notes.length <= maxChars)
            assertTrue(notes, notes.lines().size <= maxLines)
        }
    }
}
