package edu.wgu.osmt.richskill

import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.collection.CollectionEsRepo

interface QuotedSearchHelpers {
    val richSkillEsRepo: RichSkillEsRepo
    val collectionEsRepo: CollectionEsRepo

    data class SearchSetupResults(val collections: List<CollectionDoc>, val skills: List<RichSkillDoc>)

    fun quotedSearchSetup(): SearchSetupResults {
        var collection1 = TestObjectHelpers.collectionDoc(name = "Self-Management Collection", description = "Collection of Self-Management Skills")
        val collection2 = TestObjectHelpers.collectionDoc(name = "Best Self Management Collection", description = "Collection of the best Self Management Skills")
        val randomCollections = (1..10).map{ TestObjectHelpers.randomCollectionDoc() }

        val skill1 = TestObjectHelpers.richSkillDoc(name = "Self-Management", statement = "A statement for a skill").copy(collections = listOf(collection1))
        val skill2 = TestObjectHelpers.richSkillDoc(name = "Self Mis-Management", statement = "A statement for a skill")
        val skill3 = TestObjectHelpers.richSkillDoc(name = "Best Self Management", statement = "A statement for a skill").copy(collections = listOf(collection2))
        val skill4 = TestObjectHelpers.richSkillDoc(name = "Management of Selfies", statement = "A statement for a skill")

        collection1 = collection1.copy(skillIds = listOf(skill1.uuid, skill2.uuid))

        val randomSkills = (1..10).map{TestObjectHelpers.randomRichSkillDoc()}
        richSkillEsRepo.saveAll(randomSkills + listOf(skill1, skill2, skill3, skill4))
        collectionEsRepo.saveAll(listOf(collection1,collection2) + randomCollections)
        return SearchSetupResults(listOf(collection1, collection2), listOf(skill1, skill2, skill3, skill4))
    }
}
