import {PublishStatus} from "../PublishStatus"
import {IStringListUpdate} from "../richskill/ApiSkillUpdate"

export interface ICollection {
  archiveDate?: Date
  author?: string
  creationDate?: Date   // TODO: Type mismatch with dates in ISkill interface.  Why?
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
  author?: string
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
      uuid
    }: ICollection
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
  author?: string,
  skills?: IStringListUpdate
}

export class ApiCollectionUpdate {
  name?: string
  status?: PublishStatus
  author?: string
  skills?: IStringListUpdate

  constructor({name, status, author, skills}: ICollectionUpdate) {
    this.name = name
    this.status = status
    this.author = author
    this.skills = skills
  }
}
