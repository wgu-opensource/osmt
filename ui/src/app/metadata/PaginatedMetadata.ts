import { ApiJobCode } from "./job-codes/Jobcode"
import { ApiNamedReference } from "./named-references/NamedReference"

export class PaginatedMetadata implements PaginatedData<ApiJobCode | ApiNamedReference> {

  totalCount = 0
  data: ApiJobCode[] | ApiNamedReference[]  = []

  constructor(data: ApiJobCode[] | ApiNamedReference[], totalCount: number) {
    this.data = data
    this.totalCount = totalCount
  }
}
