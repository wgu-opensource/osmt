import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {ToggleButtonOption} from "../../core/toggle-button.component"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms"
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

  searchForm = new FormGroup(this.getFormDefinitions())

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)

  private searchTypeVal: SearchType = SearchType.LOCAL

  private externalLibrariesCtrl = new FormControl(new Array<string>(), Validators.required)
  private externalLibraries: Array<ApiLibrarySummary>|undefined

  private isLoadingExternalLibraries = false

  get searchType(): SearchType {
    return this.searchTypeVal
  }

  get searchCollectionsEnabled(): boolean {
    return !this.searchForm.invalid
  }

  get searchSkillsEnabled(): boolean {
    return !this.searchForm.invalid
  }

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

  get isSearchTypeToggleVisible(): boolean {
    return AppConfig.settings.externalSearchEnabled
  }

  get isLibrarySelectionVisible(): boolean {
    return AppConfig.settings.externalSearchEnabled && this.isExternalSearchType && !this.isExternalLibraryFormLoading
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
    return (this.externalLibraries) ?
      [...this.externalLibraries].map((l, i): IChoice => {
          return {
            id: l.uuid ?? i,
            name: l.uuid ?? "",
            label: l.libraryName ?? "",
            initiallySelected: (l.uuid !== undefined && this.selectedExternalLibraryIds.includes(l.uuid))
          }
        }) : []
  }

  get selectedExternalLibraryIds(): Array<string | number> {
    return (this.externalLibrariesCtrl?.value as Array<string> ?? []).map(id => id)
  }

  get selectedExternalLibraries(): ILibrarySummary[] {
    if (this.externalLibraries
        && this.externalLibrariesCtrl.value
        && this.externalLibrariesCtrl.value.size > 0
    ) {
      const selectedLibraryIds = this.selectedExternalLibraryIds
      return Array.from(this.externalLibraries).filter(l => (l.uuid && selectedLibraryIds.includes(l.uuid)))
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

  handleSearchTypeOptionClick(option: ToggleButtonOption): void {
    switch (option) {
      case ToggleButtonOption.Option2:
        this.loadExternalLibraries().subscribe((libraries) => {
          this.setSearchType(SearchType.EXTERNAL)
        })
        break
      default:
        this.setSearchType(SearchType.LOCAL)
    }
  }

  handleSelectedLibrariesChanged(choices: Set<IChoice>): void {
    this.searchForm.controls.externalLibraries.setValue([...choices].map(c => c.id))
    this.searchForm.controls.externalLibraries.markAsDirty()
  }

  handleSearchSkills(): void {
    if (!this.searchForm.invalid) {
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
  }

  handleSearchCollections(): void {
    if (!this.searchForm.invalid) {
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
  }

  protected setSearchType(searchType: SearchType): void {
    this.searchTypeVal = searchType

    if (this.searchTypeVal === SearchType.EXTERNAL) {
      this.addExternalLibrariesToForm()
    }
    else {
      this.removeExternalLibrariesToForm()
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

  private addExternalLibrariesToForm(): void {
    this.searchForm.addControl("externalLibraries", this.externalLibrariesCtrl)
  }

  private removeExternalLibrariesToForm(): void {
    this.searchForm.removeControl("externalLibraries")
  }

  private loadExternalLibraries(): Observable<Array<ApiLibrarySummary>> {
    if (this.externalLibraries) {
      return of(this.externalLibraries.map(l => l))
    }
    else {
      this.isLoadingExternalLibraries = true
      this.externalLibraries = undefined

      return this.externalSearchService.getLibraries().pipe(
        map((result) => {
          this.externalLibraries = result.libraries.map((r) => r)
          this.externalLibrariesCtrl.setValue(
            this.externalLibraries.map(l => l.uuid).filter((id): id is string => !!id)
          )
          this.isLoadingExternalLibraries = false
          return result.libraries
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
