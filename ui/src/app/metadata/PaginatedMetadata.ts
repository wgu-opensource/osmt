import { ApiJobCode } from "./job-code/Jobcode"
import { ApiNamedReference } from "./named-reference/NamedReference"
import { PaginatedData } from "../models";

export class PaginatedMetadata implements PaginatedData<ApiJobCode | ApiNamedReference> {

  totalCount = 0
  data: ApiJobCode[] | ApiNamedReference[]  = []

  constructor(data: ApiJobCode[] | ApiNamedReference[], totalCount: number) {
    this.data = data
    this.totalCount = totalCount
  }
}
