import {ApiJobCode, IJobCode} from "./job-codes/Jobcode"
import {ApiNamedReference} from "../richskill/ApiSkill"

export interface IMetadata {
  apiJobCodes?: ApiJobCode[],
  apiNamedReferences?: ApiNamedReference[]
}

export class ApiMetadata implements IMetadata {

  constructor(o?: IMetadata) {
    if (o !== undefined) {
      Object.assign(this, o)
    }
  }
}

export class PaginatedMetadata {
  totalCount = 0
  metadata: ApiJobCode[]|ApiNamedReference[] = []
  constructor(metadata: ApiJobCode[]|ApiNamedReference[], totalCount: number) {
    this.metadata = metadata
    this.totalCount = totalCount
  }
}
