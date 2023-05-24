import { ApiJobCode } from "./job-codes/Jobcode"
import { ApiNamedReference } from "./named-references/NamedReference"

export class PaginatedMetadata {
  totalCount = 0
  metadata: ApiJobCode[]|ApiNamedReference[] = []
  constructor(metadata: ApiJobCode[]|ApiNamedReference[], totalCount: number) {
    this.metadata = metadata
    this.totalCount = totalCount
  }
}
