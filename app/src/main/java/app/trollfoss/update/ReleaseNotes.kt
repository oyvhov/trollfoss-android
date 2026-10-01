package app.trollfoss.update

/*
 * GitHub release bodies are Markdown, but the parents' page shows them as plain text in a narrow
 * panel above the download button. This keeps the words, drops the markup and shortens the notes so
 * the button stays in sight. Pure Kotlin, so it is tested without Android.
 */

private val htmlComment = Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL)
private val htmlTag = Regex("</?[A-Za-z][^<>]*>")
private val image = Regex("!\\[[^\\]]*]\\([^)]*\\)")
private val link = Regex("\\[([^\\]]*)]\\([^)]*\\)")
private val heading = Regex("^#{1,6}(\\s+|$)")
private val closingHashes = Regex("\\s+#+$")
private val bullet = Regex("^[-*+]\\s+")
private val numbered = Regex("^\\d{1,3}[.)]\\s+")
private val rule = Regex("^([-*_=])(\\s*\\1){2,}$")
private val spaces = Regex("\\s+")

/** Release notes as short plain text: no images, tags or Markdown marks, cut between words. */
fun plainReleaseNotes(body: String, maxChars: Int = 360, maxLines: Int = 10): String =
    shortenNotes(markdownToPlain(body), maxChars, maxLines)

/**
 * Drops images, HTML and horizontal rules, keeps heading and link text, turns list dashes into
 * bullets and joins hard-wrapped lines back into one line per paragraph or list item.
 */
internal fun markdownToPlain(body: String): String {
    val lines = mutableListOf<String>()
    var flowing = false // the last line is a paragraph or list item that a wrapped line continues
    val text = htmlComment.replace(body, "").replace("\r\n", "\n").replace('\r', '\n')
    for (raw in text.split('\n')) {
        var line = raw.trim()
        if (rule.matches(line)) line = ""
        line = link.replace(image.replace(htmlTag.replace(line, ""), "")) { it.groupValues[1] }.trim()
        val isHeading = heading.containsMatchIn(line)
        val isBullet = !isHeading && bullet.containsMatchIn(line)
        val isItem = isBullet || (!isHeading && numbered.containsMatchIn(line))
        if (isHeading) line = closingHashes.replace(heading.replaceFirst(line, ""), "")
        if (isBullet) line = bullet.replaceFirst(line, "")
        line = spaces.replace(line.replace("**", "").replace("`", ""), " ").trim()
        when {
            line.isEmpty() -> {
                if (lines.lastOrNull()?.isNotEmpty() == true) lines += ""
                flowing = false
            }
            isHeading -> {
                lines += line
                flowing = false
            }
            isItem -> {
                lines += if (isBullet) "• $line" else line
                flowing = true
            }
            flowing -> lines[lines.lastIndex] = lines.last() + " " + line
            else -> {
                lines += line
                flowing = true
            }
        }
    }
    return lines.joinToString("\n").trim()
}

/**
 * Keeps at most [maxChars] characters and [maxLines] lines, the ellipsis included. Cuts at a line
 * break when one is near the limit (the ellipsis then gets a line of its own), otherwise between words.
 */
internal fun shortenNotes(text: String, maxChars: Int, maxLines: Int): String {
    require(maxChars >= 2 && maxLines >= 2)
    val lines = text.split('\n')
    if (text.length <= maxChars && lines.size <= maxLines) return text
    val head = if (lines.size > maxLines) lines.take(maxLines - 1).joinToString("\n").trimEnd() else text
    if (head.length <= maxChars - 2) return "$head\n…"
    val lineEnd = head.lastIndexOf('\n', maxChars - 2)
    val wordEnd = if (head.length < maxChars) head.length else head.lastIndexOf(' ', maxChars - 1)
    return when {
        lineEnd >= maxChars * 3 / 4 || (lineEnd > 0 && wordEnd <= lineEnd) -> head.substring(0, lineEnd).trimEnd() + "\n…"
        wordEnd > maxOf(lineEnd, 0) -> head.substring(0, wordEnd).trimEnd().trimEnd(',', ';', ':', '.', '–', '-').trimEnd() + "…"
        else -> head.substring(0, maxChars - 1) + "…"
    }
}
