import {Observable} from "rxjs"
import {IRelatedSkillsService} from "../../abstract.service"
import {determineFilters,PublishStatus} from "../../PublishStatus"
import {ApiSortOrder} from "../../richskill/ApiSkill"
import {ApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {ApiSearch, ISearch, PaginatedSkills} from "../../richskill/service/rich-skill-search.service"
import {FilterDropdown} from "../../models/filter-dropdown.model"
import {ISkillTableControl} from "./table.control"

export class RelatedSkillTableControl<TEntityId> implements ISkillTableControl {
  from: number
  size: number
  query: string | undefined = undefined
  sort: ApiSortOrder | undefined = undefined
  statusFilters: Set<PublishStatus>
  keywordFilters: FilterDropdown

  resultsLoaded: Observable<PaginatedSkills> | undefined = undefined
  protected results: PaginatedSkills | undefined = undefined

  protected service: IRelatedSkillsService<TEntityId>

  constructor(
    service: IRelatedSkillsService<TEntityId>,
    control: ISkillTableControl | undefined = undefined
  ) {
    this.service = service
    this.from = control?.from ?? 0
    this.size = control?.size ?? 50
    this.sort = control?.sort ?? ApiSortOrder.NameAsc
    this.query = control?.query ?? undefined
    this.statusFilters = control?.statusFilters ??
      new Set([PublishStatus.Published])

    this.keywordFilters = {
      categories: [],
      certifications: [],
      employers: [],
      alignments: [],
      keywords: [],
      occupations: [],
      standards: [],
      authors: []
    }
  }

  get currFirstSkillIndex(): number | undefined {
    return (this.hasResults) ? this.from : undefined
  }

  get currLastSkillIndex(): number | undefined {
    return (this.hasResults && this.currFirstSkillIndex !== undefined && this.currPageCount !== undefined)
      ? this.currFirstSkillIndex + this.currPageCount : undefined
  }

  get currPageCount(): number | undefined {
    return this.results?.skills?.length
  }

  get currPageNumber(): number {
    return Math.floor(this.from / this.size) + 1
  }

  get totalCount(): number {
    return this.results?.totalCount ?? 0
  }

  get totalPageCount(): number {
    return Math.ceil(this.totalCount / this.size)
  }

  get skills(): ApiSkillSummary[] {
    return this.results?.skills ?? []
  }

  get emptyResults(): boolean {
    return  !this.hasResults
  }

  get hasResults(): boolean {
    return !!(this.results?.skills.length && this.results?.skills.length > 0)
  }

  get loadingResults(): boolean {
    return !!(this.currPageCount && this.currPageCount < 0)
  }

  clearSkills() {
    this.setResults(undefined)
  }

  loadSkills(entityId: TEntityId) {
    const search = {
      filtered: this.selectedKeywordFilters
    } as ISearch

    if (this.query) {
      search.query = this.query
    }

    if (this.statusFilters.size > 0) {
      this.resultsLoaded = this.service.searchRelatedSkills(
        entityId,
        this.size,
        this.from,
        determineFilters(this.statusFilters),
        this.sort,
        new ApiSearch(search)
      )

      this.resultsLoaded.subscribe(results => this.setResults(results))
    } else {
      this.setResults(undefined)
    }
  }

  protected get selectedKeywordFilters(): any {
    const selected: any = {}

    Object.entries(this.keywordFilters)
      .filter(v => (v[1].length > 0 ))
      .forEach(v => {
        selected[v[0]] = v[1].map((i: any) => i.name ?? i.code)
      })

    return selected
  }

  protected setResults(results: PaginatedSkills | undefined) {
    this.results = results
  }
}
