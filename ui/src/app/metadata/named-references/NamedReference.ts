import {MetadataType} from "../rsd-metadata.enum"
import {Meta} from "@angular/platform-browser"

export interface INamedReference {
  id: number
  name?: string
  type?: MetadataType
  url: string
  framework: string
}

export class ApiNamedReference implements INamedReference {
  id = 0
  framework = ""
  name?: string = ""
  type?: MetadataType = MetadataType.Category
  url = ""

  constructor(o?: INamedReference) {
    if (o !== undefined) {
      Object.assign(this, o)
    }
  }



}
