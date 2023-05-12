import {MetadataType} from "../rsd-metadata.enum"

export interface INamedReference {
  id: string
  name?: string
  value: string
  type?: MetadataType
  framework: string
}

export class ApiNamedReference implements INamedReference {
  id = ""
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
