import {Component, Output, EventEmitter, OnInit, OnDestroy} from "@angular/core"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {FormField} from "../../form-field.component"
import {Subscription} from "rxjs"
import {ApiJobCode} from "../../../job-codes/Jobcode"


@Component({
  selector: "app-form-field-search-select-jobcode",
  templateUrl: "./form-field-search-select-jobcode.component.html"
})
export class FormFieldSearchSelectJobcodeComponent extends FormField implements OnInit, OnDestroy {


  @Output() currentSelection = new EventEmitter<string[]>()

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)

  queryInProgress!: Subscription
  currentlyLoading = false

  results!: ApiJobCode[] | undefined

  initialSearchComplete = false
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
    return this.initialSearchComplete && isDirty && !isEmpty && this.results !== undefined
  }

  isResultSelected(result: ApiJobCode): boolean {
    return !!this.internalSelectedResults.find(value => value === result)
  }

  performInitialSearchAndPopulation(): void {
    const value = this.control.value as string
    const codes = value.split(";").map(s => s.trim())

    this.queryInProgress = this.searchService.searchJobcodes(value).subscribe(searchResults => {
      this.internalSelectedResults = searchResults.filter(r => codes.find(c => c === r.code))
      this.control.setValue("")
      this.emitCurrentSelection()
      this.initialSearchComplete = true
    })
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
    this.emitCurrentSelection()
  }

  joinNameAndCode(apiCode: ApiJobCode): string {
    return `${apiCode.name}  ${apiCode.code}`
  }

  private emitCurrentSelection(): void {
    this.currentSelection.emit(this.internalSelectedResults.map(selectedItem => selectedItem.code))
  }
}


