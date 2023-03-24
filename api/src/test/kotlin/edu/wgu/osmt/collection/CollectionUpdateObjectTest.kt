package edu.wgu.osmt.collection

import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.db.PublishStatus
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional
internal class CollectionUpdateObjectTest @Autowired constructor(
    val collectionRepository: CollectionRepository
): SpringTest(){

    @Test
    fun `applyStatusChange() should apply Archived status`() {
        //Arrange
        val collectionUpdateObject = CollectionUpdateObject(publishStatus = PublishStatus.Archived)
        val dao: CollectionDao =
            collectionRepository.create(name = "name", user = "user", email = "user@email.edu", description = "description")!!

        //Act
        collectionUpdateObject.applyStatusChange(dao)

        //Assert
        Assertions.assertThat(dao.status).isEqualTo(PublishStatus.Archived)
        Assertions.assertThat(dao.archiveDate).isNotNull
    }

    @Test
    fun `applyStatusChange() should apply Published status`() {
        //Arrange
        val collectionUpdateObject = CollectionUpdateObject(publishStatus = PublishStatus.Published)
        val dao: CollectionDao =
            collectionRepository.create(name = "name", user = "user", email = "user@email.edu", description = "description")!!


        //Act
        collectionUpdateObject.applyStatusChange(dao)

        //Assert
        Assertions.assertThat(dao.status).isEqualTo(PublishStatus.Published)
        Assertions.assertThat(dao.publishDate).isNotNull
    }

    @Test
    fun `applyStatusChange() should return to previous draft status if no Publish date is recorded`() {
        //Arrange
        val collectionUpdateObject = CollectionUpdateObject(publishStatus = PublishStatus.Unarchived)
        val dao: CollectionDao =
            collectionRepository.create(name = "name", user = "user", email = "user@email.edu", description = "description")!!
        dao.status = PublishStatus.Archived
        dao.publishDate = null
        Assertions.assertThat(dao.status).isEqualTo(PublishStatus.Archived)


        //Act
        collectionUpdateObject.applyStatusChange(dao)

        //Assert
        Assertions.assertThat(dao.status).isEqualTo(PublishStatus.Draft)
    }

    @Test
    fun `applyStatusChange() should return to previous published status if Publish date is recorded`() {
        //Arrange
        val collectionUpdateObject = CollectionUpdateObject(publishStatus = PublishStatus.Unarchived)
        val dao: CollectionDao =
            collectionRepository.create(name = "name", user = "user", email = "user@email.edu", description = "description")!!
        dao.status = PublishStatus.Archived
        dao.publishDate = LocalDateTime.now()
        Assertions.assertThat(dao.status).isEqualTo(PublishStatus.Archived)


        //Act
        collectionUpdateObject.applyStatusChange(dao)

        //Assert
        Assertions.assertThat(dao.status).isEqualTo(PublishStatus.Published)
        Assertions.assertThat(dao.publishDate).isNotNull

    }

    @Test
    fun `applyStatusChange() should delete publish and archive date if draft status is applied and Apply draft status`() {
        //Arrange
        val collectionUpdateObject = CollectionUpdateObject(publishStatus = PublishStatus.Draft)
        val dao: CollectionDao =
            collectionRepository.create(name = "name", user = "user", email = "user@email.edu", description = "description")!!
        dao.publishDate = LocalDateTime.now()
        dao.archiveDate = LocalDateTime.now()


        //Act
        collectionUpdateObject.applyStatusChange(dao)

        //Assert
        Assertions.assertThat(dao.status).isEqualTo(PublishStatus.Draft)
        Assertions.assertThat(dao.publishDate).isNull()
        Assertions.assertThat(dao.archiveDate).isNull()
    }

    @Test
    fun `applyStatusChange() should Apply workspace status`() {
        //Arrange
        val collectionUpdateObject = CollectionUpdateObject(publishStatus = PublishStatus.Workspace)
        val dao: CollectionDao =
            collectionRepository.create(name = "name", user = "user", email = "user@email.edu", description = "description")!!
        dao.publishDate = LocalDateTime.now()
        dao.archiveDate = LocalDateTime.now()


        //Act
        collectionUpdateObject.applyStatusChange(dao)

        //Assert
        Assertions.assertThat(dao.status).isEqualTo(PublishStatus.Workspace)
        Assertions.assertThat(dao.publishDate).isNull()
        Assertions.assertThat(dao.archiveDate).isNull()
    }

}