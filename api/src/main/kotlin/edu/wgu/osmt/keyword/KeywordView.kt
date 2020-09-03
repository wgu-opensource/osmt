package edu.wgu.osmt.keyword

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView
import edu.wgu.osmt.richskill.RichSkillView

class KeywordView {
    interface PublicDetailView {}
    interface PrivateDetailView : PublicDetailView {}
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class KeywordDTO(private val kw: Keyword) {
    @get:JsonView(RichSkillView.PublicDetailView::class)
    val name: String?
        get() = kw.value

    @get:JsonView(RichSkillView.PublicDetailView::class)
    val id: String?
        get() = kw.uri
}
