import {Component, Input, OnDestroy, OnInit} from "@angular/core"
import {FormControl} from "@angular/forms"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {Observable, Subscription} from "rxjs"
import {AbstractFormField} from "../abstract-form-field.component"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"

@Component({
  selector: "app-abstract-form-field-search-select",
  template: ""
})
export abstract class AbstractFormFieldSearchSelect<TSearch, TValue>
  extends AbstractFormField<TValue|null> implements OnInit, OnDestroy {

  @Input() createNonExisting = false

  searchControl: FormControl = new FormControl("")

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)
  iconCheck = SvgHelper.path(SvgIcon.CHECK)

  queryInProgress!: Subscription
  currentlyLoading = false
  results!: TSearch[] | undefined

  protected searchService: KeywordSearchService

  constructor(searchService: KeywordSearchService) {
    super()
    this.searchService = searchService
  }

  ngOnInit() {
    this.searchControl.valueChanges.subscribe((v: string) => this.onSearchValueChange(v) )
  }

  ngOnDestroy() {
    this.queryInProgress?.unsubscribe()
  }

  abstract areSearchResultsEqual(result1: TSearch, result2: TSearch): boolean
  abstract isResultSelected(result: TSearch): void
  abstract isSearchResultType(result: any): result is TSearch
  abstract labelFor(result: TSearch): string|null
  abstract selectResult(result: TSearch): void
  protected abstract callSearchService(text: string): Observable<TSearch[]>
  protected abstract handleClearSearchClicked(): void
  protected abstract handleSearchResultClicked(result: TSearch): void
  protected abstract searchResultFromString(value: string): TSearch|undefined

  get emptyValue(): TValue|null { return null }

  get isSearchDirty(): boolean {
    return this.searchControl.dirty
  }

  get isSearchEmpty(): boolean {
    return this.searchControlValue.length <= 0
  }

  get searchControlValue(): string {
    return this.searchControl.value?.trim() ?? ""
  }

  set searchControlValue(value: string) {
    this.setSearchControlValue(value, true)
    this.searchControl.markAsDirty()
    this.searchControl.markAsTouched()
  }

  get searchControlResultValue(): TSearch | undefined {
    const searchResult = this.searchResultFromString(this.searchControlValue)

    if (searchResult) {
      const existingResult =this.results?.find((r: TSearch) => this.areSearchResultsEqual(r, searchResult))
      return existingResult ?? (this.createNonExisting) ? searchResult : undefined
    }

    return undefined
  }

  get showResults(): boolean {
    return this.isSearchDirty && !this.isSearchEmpty && this.results !== undefined
  }

  clearSearch(): void {
    this.clearSearchResults()
    this.setSearchControlValue("", false)
    this.searchControl.markAsPristine()
  }

  clearSearchResults(): void {
    this.results = undefined
  }

  makeHtmlId(result: TSearch): string {
    return this.labelFor(result)?.replace(new RegExp("\\W"), "") ?? ""
  }

  protected performSearch(text: string): void {
    if (!text) {
      return // no search to perform
    }
    this.currentlyLoading = true

    if (this.queryInProgress) {
      this.queryInProgress.unsubscribe() // unsub to existing query first
    }

    this.queryInProgress = this.callSearchService(text).subscribe(searchResults => {
      this.results = searchResults
      this.currentlyLoading = false
    })
  }

  protected selectSearchValue() {
    let searchResult = this.searchControlResultValue
    searchResult = searchResult ?? (this.createNonExisting)
      ? this.searchResultFromString(this.searchControlValue) : undefined

    if (searchResult) {
      this.selectResult(searchResult)
      this.clearSearchResults()
    } else if (this.createNonExisting) {
      this.clearValue()
      this.clearSearchResults()
    }
  }

  protected setSearchControlValue(value: string|null, emitEvent: boolean = true) {
    this.searchControl.setValue(value ?? "", { emitEvent: emitEvent })
  }

  onClearSearchClicked(): void {
    this.handleClearSearchClicked()
  }

  onEnterKeyDown(event: any): boolean {
    this.selectSearchValue()
    return false
  }

  onSearchResultClicked(result: TSearch): void {
    this.handleSearchResultClicked(result)
  }

  protected onSearchValueChange(newValue: string): void {
    this.handleSearchValueChange(newValue)
  }

  protected handleSearchValueChange(newValue: string): void {
    this.performSearch(newValue)
  }
}
