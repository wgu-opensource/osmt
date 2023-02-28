import {PublishStatus} from "../PublishStatus"
import {IStringListUpdate} from "../richskill/ApiSkillUpdate"

export interface ICollection {
  archiveDate?: Date
  author?: string
  creationDate?: Date
  creator: string
  id: string
  name: string
  description?: string
  workspaceOwner?: string
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
  description?: string
  publishDate?: Date
  workspaceOwner?: string
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
      description,
      workspaceOwner,
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
    this.description = description
    this.workspaceOwner = workspaceOwner
    this.publishDate = publishDate
    this.skills = skills
    this.status = status
    this.updateDate = updateDate
    this.uuid = uuid
  }
}

export interface ICollectionUpdate {
  name?: string,
  description?: string,
  status?: PublishStatus,
  author?: string,
  skills?: IStringListUpdate
}

export class ApiCollectionUpdate {
  name?: string
  description?: string
  status?: PublishStatus
  author?: string
  skills?: IStringListUpdate
  workSpaceOwner?: string

  constructor({name, description, status, author, skills}: ICollectionUpdate) {
    this.name = name
    this.description = description
    this.status = status
    this.author = author
    this.skills = skills
  }
}
