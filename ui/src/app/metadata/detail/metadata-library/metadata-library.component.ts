import {Component, OnInit} from "@angular/core"
import {MetadataType} from "../../rsd-metadata.enum"
import {Observable} from "rxjs"
import {PaginatedMetadata} from "../../IMetadata"

@Component({
  selector: "app-metadata-library",
  templateUrl: "./metadata-library.component.html"
})
export class MetadataLibraryComponent implements OnInit {

  title = "Metadata"
  selectedMetadata = MetadataType.Category
  matchingQuery?: string[]

  from = 0
  size = 50

  resultsLoaded: Observable<PaginatedMetadata> | undefined
  constructor() { }

  results: PaginatedMetadata | undefined

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get metadataCountLabel(): string {
    if (this.totalCount > 0)  {
      return `${this.totalCount} ${this.selectedMetadata}${this.totalCount > 1 ? "s" : ""}`
    }
    return `0 ${this.selectedMetadata}s`
  }
  get firstRecordNo(): number {
    return this.from + 1
  }
  get lastRecordNo(): number {
    return Math.min(this.from + this.curPageCount, this.totalCount)
  }

  get totalPageCount(): number {
    return Math.ceil(this.totalCount / this.size)
  }
  get currentPageNo(): number {
    return Math.floor(this.from / this.size) + 1
  }

  get curPageCount(): number {
    return this.results?.metadata.length ?? 0
  }

  getMobileSortOptions(): {[s: string]: string} {
    return {
      "skill.asc": "RSD Name (ascending)",
      "skill.desc": "RSD Name (descending)",
    }
  }

  get emptyResults(): boolean {
    return this.curPageCount < 1
  }
  get isJobCodeDataSelected(): boolean {
    return  this.selectedMetadata === MetadataType.JobCode && !this.emptyResults
  }

  ngOnInit(): void {
  }

}
