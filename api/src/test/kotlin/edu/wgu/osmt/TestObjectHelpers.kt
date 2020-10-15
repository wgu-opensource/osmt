package edu.wgu.osmt

import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.streams.asSequence

object TestObjectHelpers {

    fun keywordGenerator(n: Int, type: KeywordTypeEnum): List<Keyword> {
        val keywords = (0..n).toList().map {
            val chars = "abcdefghijklmnopqrstuvwxyz"
            val word = Random().ints(10, 0, chars.length).asSequence().map(chars::get).joinToString("").capitalize()
            Keyword(null, LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC), type, word, null)
        }
        return keywords
    }
}
