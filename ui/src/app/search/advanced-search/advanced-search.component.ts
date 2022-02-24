import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {ToggleButtonOption} from "../../core/toggle-button.component"
import {AbstractControl, FormControl, FormGroup} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {urlValidator} from "../../validators/url.validator"
import {SearchService} from "../search.service"
import {ApiAdvancedSearch} from "../../richskill/service/rich-skill-search.service"
import {INamedReference} from "../../richskill/ApiSkill"
import {Title} from "@angular/platform-browser"
import {Whitelabelled} from "../../../whitelabel"
import {ExternalSearchService} from "../external/external-search.service"
import {ApiLibrarySummary, ILibrarySummary} from "../external/api/ApiLibrary"
import {IChoice} from "../../form/form-field-choice.component"
import {Observable, of} from "rxjs"
import {map} from "rxjs/operators"

enum SearchType {
  LOCAL,
  EXTERNAL
}

@Component({
  selector: "app-advanced-search",
  templateUrl: "./advanced-search.component.html"
})
export class AdvancedSearchComponent extends Whitelabelled implements OnInit {
  readonly SvgIcon = SvgIcon

  searchType: SearchType = SearchType.LOCAL

  searchForm = new FormGroup(this.getFormDefinitions())

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)

  private externalLibraries: Set<ApiLibrarySummary>|undefined

  private isLoadingExternalLibraries = false

  get searchTypeToggleSelectedOption(): ToggleButtonOption|null {
    switch (this.searchType) {
      case SearchType.LOCAL:
        return ToggleButtonOption.Option1
      case SearchType.EXTERNAL:
        return ToggleButtonOption.Option2
      default:
        return null
    }
  }

  get isLocalSearchType(): boolean {
    return (this.searchType === SearchType.LOCAL)
  }

  get isExternalSearchType(): boolean {
    return (this.searchType === SearchType.EXTERNAL)
  }

  get isExternalLibraryFormLoading(): boolean {
    return (this.isLoadingExternalLibraries)
  }

  get externalLibraryChoices(): IChoice[] {
    const selectedLibraryIds = this.selectedExternalLibraryIds

    return (this.externalLibraries) ?
      [...this.externalLibraries].map((l, i): IChoice => {
          return {
            id: l.uuid ?? i,
            name: l.uuid ?? "",
            label: l.libraryName ?? "",
            initiallySelected: (l.uuid !== undefined && selectedLibraryIds.has(l.uuid))
          }
        }) : []
  }

  get selectedExternalLibraryIds(): Set<string | number> {
    return new Set(
      ([...this.searchForm.controls.externalLibraries?.value?.values()] ?? []).map((c: IChoice) => c.id)
    )
  }

  get selectedExternalLibraries(): ILibrarySummary[] {
    if (this.externalLibraries
        && this.searchForm.value.externalLibraries
        && this.searchForm.value.externalLibraries.size > 0
    ) {
      const selectedLibraryIds = this.selectedExternalLibraryIds
      return Array.from(this.externalLibraries).filter(l => (l.uuid && selectedLibraryIds.has(l.uuid)))
    }
    else {
      return []
    }
  }

  constructor(
    private searchService: SearchService,
    private externalSearchService: ExternalSearchService,
    protected titleService: Title
  ) {
    super()
  }

  ngOnInit(): void {
    this.titleService.setTitle(`Advanced Search | ${this.whitelabel.toolName}`)
  }

  getFormDefinitions(): {[key: string]: AbstractControl} {
    const fields = {
      externalLibraries: new FormControl(new Set<ApiLibrarySummary>()),
      name: new FormControl(""),
      author: new FormControl(""),
      skillStatement: new FormControl(""),
      category: new FormControl(""),
      keywords: new FormControl(""),
      standards: new FormControl(""),
      certifications: new FormControl(""),
      occupations: new FormControl(""),
      employers: new FormControl(""),
      alignments: new FormControl("", urlValidator),
      collectionName: new FormControl(""),
    }
    return fields
  }

  handleSearchTypeOptionClick(option: ToggleButtonOption): void {
    switch (option) {
      case ToggleButtonOption.Option2:
        this.loadExternalLibraries().subscribe((libraries) => {
          this.searchType = SearchType.EXTERNAL
        })
        break
      default:
        this.searchType = SearchType.LOCAL
    }
  }

  handleSearchSkills(): void {
    switch (this.searchType) {
      case SearchType.EXTERNAL:
        return this.externalSearchService.advancedSkillSearch(
          this.collectFieldData(),
          this.selectedExternalLibraries
        )
      default:
        return this.searchService.advancedSkillSearch(this.collectFieldData())
    }
  }

  handleSearchCollections(): void {
    switch (this.searchType) {
      case SearchType.EXTERNAL:
        return this.externalSearchService.advancedCollectionSearch(
          this.collectFieldData(true),
          this.selectedExternalLibraries
        )
      default:
        return this.searchService.advancedCollectionSearch(this.collectFieldData(true))
    }
  }

  private collectFieldData(isSearchingCollections: boolean = false): ApiAdvancedSearch {
    const form = this.searchForm.value

    const name: string|undefined = (form.name && form.name !== "") ? form.name : undefined
    const author: string = form.author
    const skillStatement: string = form.skillStatement
    const category: string = form.category
    const keywords = this.tokenizeString(form.keywords)
    const standards = this.prepareNamedReferences(form.standards)
    const certifications = this.prepareNamedReferences(form.certifications)
    const occupations = this.tokenizeString(form.occupations)
    const employers = this.prepareNamedReferences(form.employers)
    const alignments = this.prepareNamedReferences(form.alignments)
    const collectionName = form.collectionName

    // if a property would be falsey, then completely omit it
    return {
      ...isSearchingCollections && name && { collectionName: name },
      ...!isSearchingCollections && name && { skillName: name },
      ...author && { author },
      ...skillStatement && { skillStatement },
      ...category && { category},
      ...form.keywords && { keywords },
      ...form.standards && { standards },
      ...form.certifications && { certifications },
      ...form.occupations && { occupations },
      ...form.employers && { employers },
      ...form.alignments && { alignments },
      ...collectionName && { collectionName }
    }
  }

  scrubReference(value: string): INamedReference | undefined {
    value = value.trim()
    return (value.length > 0) ? {name: value} : undefined
  }

  prepareNamedReferences(value: string, token: string = ";"): INamedReference[] | undefined {
    return this.tokenizeString(value, token)?.map(v => ({name: v})) || undefined
  }

  // used for advance search to tokenize string fields
  tokenizeString(value: string, token: string = ";"): string[] | undefined {
    return value
        .split(token)
        .map(v => v.trim())
        .filter(v => v.length > 0)
      || undefined
  }

  showAuthor(): boolean {
    return AppConfig.settings.editableAuthor
  }

  getSemicolonHelpMessage(): string {
    return "Use a semicolon to separate multiple entries in a field."
  }

  getOccupationHelpMessage(): string {
    return "BLS or O*NET job names or codes"
  }

  private loadExternalLibraries(): Observable<Set<ApiLibrarySummary>> {
    if (this.externalLibraries) {
      return of(new Set(this.externalLibraries))
    }
    else {
      this.isLoadingExternalLibraries = true
      this.externalLibraries = undefined

      return this.externalSearchService.getLibraries().pipe(
        map((result) => {
          this.externalLibraries = new Set(result.libraries)
          this.searchForm.controls.externalLibraries.setValue(new Set(this.externalLibraryChoices))
          this.isLoadingExternalLibraries = false
          return new Set(result.libraries)
        })
      )
    }
  }

  private resetExternalLibraries(): void {
    this.isLoadingExternalLibraries = false
    this.externalLibraries = undefined
    this.searchForm.controls.externalLibraries.setValue(null)
  }
}
