import {MetadataType} from "../rsd-metadata.enum"

export interface INamedReference {
  name?: string
  value: string
  type?: MetadataType
  framework: string
}

export class ApiNamedReference implements INamedReference {
  framework = ""
  name = ""
  type = MetadataType.Category
  value = ""

  constructor(o?: INamedReference) {
    if (o !== undefined) {
      Object.assign(this, o)
    }
  }

}
