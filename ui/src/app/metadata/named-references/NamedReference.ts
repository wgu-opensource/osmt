import {MetadataType} from "../rsd-metadata.enum"
import {Meta} from "@angular/platform-browser"
import {IJobCode, JobCodeLevel} from "../job-codes/Jobcode";

export interface NamedReferenceInterface {
  id: number
  name?: string
  type?: MetadataType
  url: string
  framework: string
}

export class ApiNamedReference implements NamedReferenceInterface {
  id = 0
  framework = ""
  name?: string = ""
  type?: MetadataType = MetadataType.Category
  url = ""

  constructor(o?: NamedReferenceInterface) {
    if (o !== undefined) {
      Object.assign(this, o)
    }
  }
}

export interface INamedReferenceUpdate {
  name?: string
  type?: MetadataType
  url: string
  framework: string
}

export class ApiNamedReferenceUpdate implements INamedReferenceUpdate {
  framework = ""
  name?: string = ""
  type?: MetadataType = MetadataType.Category
  url = ""

  constructor({framework, name, type, url}: INamedReferenceUpdate) {
    this.framework = framework
    this.name = name
    this.type = type
    this.url = url
    }
}


