import {MetadataType} from "../rsd-metadata.enum"

export interface NamedReferenceInterface {
  id: number
  name?: string
  type?: MetadataType
  url: string
  framework: string
  skillCount: number
}

export class ApiNamedReference implements NamedReferenceInterface {
  id = 0
  framework = ""
  name?: string = ""
  type?: MetadataType = MetadataType.Category
  url = ""
  skillCount: number = 0

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
  skillCount = 0

  constructor({framework, name, type, url}: INamedReferenceUpdate) {
    this.framework = framework
    this.name = name
    this.type = type
    this.url = url
    this.skillCount = 0
    }
}


