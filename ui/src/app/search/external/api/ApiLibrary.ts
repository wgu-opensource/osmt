export interface ILibrarySummary {
  uuid?: string
  libraryName?: string
}

export class ApiLibrarySummary {
  uuid?: string
  libraryName?: string

  constructor(summary: ILibrarySummary) {
    this.uuid = summary.uuid
    this.libraryName = summary.libraryName
  }
}

export class PaginatedLibraries {
  totalCount = 0
  libraries: ILibrarySummary[] = []
  constructor(libraries: ILibrarySummary[], totalCount: number) {
    this.libraries = libraries
    this.totalCount = totalCount
  }
}
