import {PublishStatus} from "../PublishStatus"
import {INamedReference} from "../richskill/ApiSkill"
import {IStringListUpdate} from "../richskill/ApiSkillUpdate"
import {IApiSkillSummary} from "../richskill/ApiSkillSummary"

export interface ICollection {
  archiveDate?: Date
  author?: INamedReference
  creationDate?: Date
  creator: string
  id: string
  name: string
  publishDate?: Date
  skills: string[]
  status: PublishStatus
  updateDate?: Date
  uuid: string
}

export class ApiCollection {
  archiveDate?: Date
  author?: INamedReference
  creationDate?: Date
  creator: string
  id: string
  name: string
  publishDate?: Date
  skills: string[]
  status: PublishStatus
  updateDate?: Date
  uuid: string

  constructor(
    {
    archiveDate,
    author,
    creationDate,
    creator,
    id,
    name,
    publishDate,
    skills,
    status,
    updateDate,
    uuid}: ICollection
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

export interface ICollectionUpdate {
  name?: string,
  status?: PublishStatus,
  author?: INamedReference,
  skills?: IStringListUpdate
}
