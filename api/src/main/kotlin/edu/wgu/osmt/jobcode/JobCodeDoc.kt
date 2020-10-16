package edu.wgu.osmt.jobcode

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "jobcode", createIndex = true)
data class JobCodeDoc(
    @Field
    @Id
    val id: Long,

    @Field(type = FieldType.Keyword)
    val major: String? = null,             // bls major category name

    @Field(type = FieldType.Keyword)
    val minor: String? = null,             // bls minor category name

    @Field(type = FieldType.Keyword)
    val broad: String? = null,             // bls broad category name

    @Field(type = FieldType.Keyword)
    val detailed: String? = null,          // bls detailed (for o*net level codes -- blank for bls detailed)

    @Field(type = FieldType.Keyword)
    val code: String,                           // bls detailed code or a o*net code: XX-XXXX or XX-XXXX.XX

    @Field
    val name: String? = null                   // human readable label
)
