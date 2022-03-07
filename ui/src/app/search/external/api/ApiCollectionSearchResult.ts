import {ICollectionSummary} from "../../../richskill/ApiSkillSummary";

export interface ICollectionSearchResult {
  collection: ICollectionSummary
}

export class ApiCollectionSearchResult implements ICollectionSearchResult{
  collection: ICollectionSummary

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
