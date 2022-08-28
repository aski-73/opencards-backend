package net.aveyon.opencards_backend.service

import java.lang.StringBuilder
import java.util.Locale

/**
 * Takes a .txt or a string as an input and processes it into a CSV.
 *
 */
class TextFileProcessorService {
    companion object {
        private const val DELIMITER = "|";

        fun countLines(textContent: String, delimiter: String = DELIMITER): Int {
            return textContent.split(delimiter).size
        }
    }

    /**
     * Each line of [textContent] is split into [languages].size columns. Each column representing
     * a word in a specific language.
     * Preprocessing is made in order to normalize the file: Uniform split character "-"
     */
    fun process(languages: Array<String>, textContent: String): String {
        // Preprocessing
        var preprocessedContent = preProcess(textContent)

        var csv = StringBuilder()

        // Build Header
        languages.forEach { csv.append(it).append(",") }
        csv.removeSuffix(",")

        // Build Rest
        var lines = preprocessedContent.split("\\n")

        lines.forEach {
            var columns = it.split("-")
            if (columns.size > 1) { // ignore headers since they have size 1
                for (i in languages.indices) {
                    if (i < columns.size - 1)
                        csv.append(columns[i]).append(",")
                }
                csv.removeSuffix(",")
            }
        }


        return csv.toString()
    }

    /**
     * First language is the lead language (quizlet front card).
     * All others are placed in the second column (Quizlet back of card).
     * @param questionAnswerDelimiter Delimiter between question and answer
     */
    fun processQuizlet(
        languages: Array<Locale>, textContent: String,
        questionAnswerDelimiter: String, showLinesCount: Boolean, cardDelimiter: String = DELIMITER
    ): String {
        // Preprocessing
        val preprocessedContent = preProcess(textContent)

        val quizlet = StringBuilder()

        val lines = preprocessedContent.split("\n")

        // Since we ignore lines whose columns start with "..." not all lines are considered in the processing.
        // However with this variable we keep track of the real line count.
        var realLinesCount = 1

        lines.forEach() { it ->
            val columns = it.split("-")
            if (columns.size == languages.size) { // ignore headers since they have size 1

                // ignore line if any of the column contains ".." in the beginning. This means that there is
                // no translation for this word.
                val noTranslation = columns.any { s -> s.startsWith("...") || s.startsWith("..") || s.startsWith("…") }
                if (noTranslation) {
                    return@forEach
                }

                if (showLinesCount) {
                    quizlet.append(realLinesCount).append(". ")
                }

                // Add leading language
                quizlet
                    .append(columns[0].trim())
                    .append(questionAnswerDelimiter)

                // add all other languages separated by escaped carriage return
                columns
                    .drop(1)
                    .filter { cellText -> cellText.trim().isNotEmpty() }
                    .map { cellText -> cellText.trim() }
                    .forEachIndexed() { index, cellText ->
                        quizlet
                            .append(("[" + languages[index + 1].language + "] "))
                            .append(cellText)
                        // Prevent leading carriage-return
                        if (index + 1 < languages.size - 1) {
                            quizlet.append("")
                        }
                    }

                quizlet.append(cardDelimiter)

                realLinesCount++
            }
        }

        return quizlet.toString()
    }

    fun preProcess(textContent: String): String {
        return textContent.replace("–", "-")
    }
}
