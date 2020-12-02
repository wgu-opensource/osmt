package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.keyword.KeywordRepository
import org.springframework.beans.factory.annotation.Autowired

class EsKeywordTest: SpringTest(), HasDatabaseReset  {
    @Autowired
    lateinit var keywordRepository: KeywordRepository

    @Autowired
    lateinit var esKeywordRepository: EsKeywordRepository


}
