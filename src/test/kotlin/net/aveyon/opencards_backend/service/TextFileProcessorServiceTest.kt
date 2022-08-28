package net.aveyon.opencards_backend.service

import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale

class TextFileProcessorServiceTest {

    private lateinit var processor: TextFileProcessorService

    @Before
    fun setUp() {
        processor = TextFileProcessorService()
    }

    @Test
    fun splitsTextIntoCsv() {
        // GIVEN
        val languages = arrayOf("English", "Vietnamese", "Russian")
        val targetFilePath = TextFileProcessorServiceTest::class.java.classLoader.getResource("./words.txt")?.path
        val targetFile = File(targetFilePath!!)
        // WHEN
        val txt = processor.process(languages, targetFile.readText(StandardCharsets.UTF_8))

        // THEN
        assert(txt.isNotEmpty())
        print(txt)
    }

    @Test
    fun createsQuizletReadyTextFile() {
        // GIVEN
        val languagesLocale = arrayOf(Locale("vi"), Locale("en"), Locale("ru"))
        val sourceWordFilePath = TextFileProcessorServiceTest::class.java.classLoader.getResource("./words.txt")?.path
        val sourceWordFile = File(sourceWordFilePath!!)
        val cardDelimiter  = "|";

        // WHEN
        val txt = processor.processQuizlet(
            languagesLocale, sourceWordFile.readText(StandardCharsets.UTF_8),
            "»", false, cardDelimiter
        )

        // THEN
        assert(txt.isNotEmpty())
        print(txt)
        println("Anz: " + TextFileProcessorService.countLines(txt, cardDelimiter) )

        // Okay
        File("/tmp/out.txt").writeText(txt)
    }
}
