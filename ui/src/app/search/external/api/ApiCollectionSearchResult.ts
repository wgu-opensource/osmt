import {ApiCollectionSummary} from "../../../richskill/ApiSkillSummary";

export interface ICollectionSearchResult {
  collection: ApiCollectionSummary
}

export class ApiCollectionSearchResult implements ICollectionSearchResult{
  collection: ApiCollectionSummary

  constructor(collectionSearchResult: ICollectionSearchResult) {
    this.collection = collectionSearchResult.collection
  }
}

export class PaginatedCollectionSearchResults {
  totalCount = 0
  results: ICollectionSearchResult[] = []

  constructor(results: ICollectionSearchResult[], totalCount: number) {
    this.results = results
    this.totalCount = totalCount
  }
}
