import { ApiMetadata } from "./IMetadata"

export interface IMetadataSummary {
  count?: number
  metadata?: ApiMetadata
}

export class ApiMetadataSummary implements IMetadataSummary {
  count?: number
  metadata?: ApiMetadata
  constructor({count, metadata}: IMetadataSummary)  {
    this.count = count
    this.metadata = metadata
  }
}
