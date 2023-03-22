package edu.wgu.osmt.io.xlsx

import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillAndCollections
import edu.wgu.osmt.richskill.RichSkillDescriptor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

internal class RichSkillXlsxExportTest @Autowired constructor(
    appConfig: AppConfig
) : SpringTest() {

    val richSkillXlsExport = RichSkillXlsxExport(appConfig)

    val collection = Collection(
        id = 123,
        creationDate = LocalDateTime.now(),
        name = "name",
        status = PublishStatus.Draft,
        updateDate = LocalDateTime.now(),
        uuid = UUID.randomUUID().toString()
    )

    @Test
    fun`columnTranslations() should retrieve only 14 columns if no Alignments are present`() {
        //Arrange
        val rsd =  RichSkillDescriptor(
            id = 123,
            creationDate = LocalDateTime.now(),
            name = "name",
            statement = "statement",
            updateDate = LocalDateTime.now(),
            uuid = UUID.randomUUID().toString()
        )

        //Act
        val result = richSkillXlsExport.columnTranslations(listOf(RichSkillAndCollections(rsd, setOf(collection))))

        //Assert
        Assertions.assertThat(result).hasSize(14)
    }

    @Test
    fun`columnTranslations() should retrieve 17 columns if Alignments are present`() {
        //Arrange
        val rsd =  RichSkillDescriptor(
            id = 123,
            creationDate = LocalDateTime.now(),
            keywords = listOf(
                Keyword(
                    id = 123,
                    creationDate = LocalDateTime.now(),
                    type = KeywordTypeEnum.Alignment,
                    updateDate = LocalDateTime.now()
                )),
            name = "name",
            statement = "statement",
            updateDate = LocalDateTime.now(),
            uuid = UUID.randomUUID().toString()
        )

        //Act
        val result = richSkillXlsExport.columnTranslations(listOf(RichSkillAndCollections(rsd, setOf(collection))))

        //Assert
        Assertions.assertThat(result).hasSize(17)
    }

    @Test
    fun`toXls() should convert columns to xls object without Alignments`() {
        
    }
}