import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {ToggleButtonOption} from "../../core/toggle-button.component"
import {AbstractControl, FormControl, FormGroup} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {urlValidator} from "../../validators/url.validator"
import {SearchService} from "../search.service"
import {ApiAdvancedSearch} from "../../richskill/service/rich-skill-search.service"
import {INamedReference} from "../../richskill/ApiSkill";
import {Title} from "@angular/platform-browser";
import {Whitelabelled} from "../../../whitelabel";
import {SearchHubService} from "../searchhub/searchhub.service";
import {ApiLibrarySummary} from "../searchhub/ApiLibrary";

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

  skillForm = new FormGroup(this.getFormDefinitions())

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)

  externalLibraries?: ApiLibrarySummary[]

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

  get isExternalLibraryFormLoading(): boolean {
    return (this.isLoadingExternalLibraries)
  }

  constructor(
    private searchService: SearchService,
    private searchHubService: SearchHubService,
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

  handleSearchTypeOptionClick(option: ToggleButtonOption): void {
    switch (option) {
      case ToggleButtonOption.Option2:
        this.searchType = SearchType.EXTERNAL
        this.loadExternalLibraries()
        break
      default:
        this.searchType = SearchType.LOCAL
    }
  }

  handleSearchSkills(): void {
    this.searchService.advancedSkillSearch(this.collectFieldData())
  }

  handleSearchCollections(): void {
    this.searchService.advancedCollectionSearch(this.collectFieldData(true))
  }

  private collectFieldData(isSearchingCollections: boolean = false): ApiAdvancedSearch {
    const form = this.skillForm.value

    const name: string = form.name
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
      ...isSearchingCollections && { collectionName: name },
      ...!isSearchingCollections && { skillName: name },
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

  private loadExternalLibraries(): void {
    this.isLoadingExternalLibraries = true

    this.searchHubService.getLibraries()
      .subscribe((libraries) => {
        this.externalLibraries = libraries.libraries
        this.isLoadingExternalLibraries = false
      })
  }
}
