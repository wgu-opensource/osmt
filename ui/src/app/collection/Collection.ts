import {PublishStatus} from "../PublishStatus"
import {INamedReference} from "../richskill/ApiSkill"
import {IStringListUpdate} from "../richskill/ApiSkillUpdate"
import {IApiSkillSummary} from "../richskill/ApiSkillSummary"

export interface ICollection {
  id: string
  uuid: string
  creator: string
  status: PublishStatus
  creationDate: Date
  updateDate: Date
  publishDate: Date
  archiveDate: Date
  name: string
  author: INamedReference
  skills: IApiSkillSummary[]
}

export class Collection {
  archiveDate: Date
  author: INamedReference
  creationDate: Date
  creator: string
  id: string
  name: string
  publishDate: Date
  skills: IApiSkillSummary[]
  status: PublishStatus
  updateDate: Date
  uuid: string

  constructor(
    archiveDate: Date,
    author: INamedReference,
    creationDate: Date,
    creator: string,
    id: string,
    name: string,
    publishDate: Date,
    skills: IApiSkillSummary[],
    status: PublishStatus,
    updateDate: Date,
    uuid: string
  ) {
    this.archiveDate = archiveDate
    this.author = author
    this.creationDate = creationDate
    this.creator = creator
    this.id = id
    this.name = name
    this.publishDate = publishDate
    this.skills = skills
    this.status = status
    this.updateDate = updateDate
    this.uuid = uuid
  }
}

export interface CollectionUpdate {
  name: string,
  status: PublishStatus,
  author: INamedReference,
  skills: IStringListUpdate
}
