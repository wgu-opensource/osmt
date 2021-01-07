import {Component, Output, EventEmitter, OnInit, OnDestroy, Input} from "@angular/core"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {FormField} from "../../form-field.component"
import {Subscription} from "rxjs"
import {ApiJobCode, IJobCode} from "../../../job-codes/Jobcode"


@Component({
  selector: "app-form-field-search-select-jobcode",
  templateUrl: "./form-field-search-select-jobcode.component.html"
})
export class FormFieldSearchSelectJobcodeComponent extends FormField implements OnInit, OnDestroy {

  @Input() existing?: IJobCode[]

  @Output() currentSelection = new EventEmitter<string[]>()

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)
  iconCheck = SvgHelper.path(SvgIcon.CHECK)

  queryInProgress!: Subscription
  currentlyLoading = false

  results!: ApiJobCode[] | undefined

  internalSelectedResults: ApiJobCode[] = []

  constructor(protected searchService: KeywordSearchService) {
    super()
  }

  ngOnInit(): void {
    this.control.valueChanges.subscribe(next => { this.performSearch(next) })
    this.performInitialSearchAndPopulation()
  }

  ngOnDestroy(): void {
    this.queryInProgress?.unsubscribe()
  }

  get showResults(): boolean {
    const isEmpty = this.valueFromControl?.trim()?.length <= 0
    const isDirty = this.control.dirty
    return isDirty && !isEmpty && this.results !== undefined
  }

  isResultSelected(result: ApiJobCode): boolean {
    return !!this.internalSelectedResults.find(value => value.code === result.code)
  }

  performInitialSearchAndPopulation(): void {
    if (this.existing) {
      this.internalSelectedResults = this.existing.map(it => new ApiJobCode(it))
      this.control.setValue("")
      this.emitCurrentSelection()
    }
  }

  // This component calls a different api than the others, otherwise is just another search field with multi-select
  performSearch(text: string): void {
    if (!text) {
      return // no search to perform
    }
    this.currentlyLoading = true

    if (this.queryInProgress) {
      this.queryInProgress.unsubscribe() // unsub to existing query first
    }

    this.queryInProgress = this.searchService.searchJobcodes(text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.code && !!r.name)
        this.currentlyLoading = false
      })
  }

  selectResult(result: ApiJobCode): void {
    if (!this.isResultSelected(result)) {
      this.internalSelectedResults.push(result)
    }
    this.emitCurrentSelection()
  }

  unselectResult(result: ApiJobCode): void {
    this.internalSelectedResults = this.internalSelectedResults.filter(r => r !== result)
    this.control.markAsDirty()
    this.emitCurrentSelection()
  }

  joinNameAndCode(apiCode: ApiJobCode): string {
    return `${apiCode.name}  ${apiCode.code}`
  }

  private emitCurrentSelection(): void {
    this.currentSelection.emit(this.internalSelectedResults.map(selectedItem => selectedItem.code))
  }
}


