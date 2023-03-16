import {Component, OnInit} from "@angular/core"
import {Observable} from "rxjs"
import {AbstractFormFieldSearchSelect} from "./abstract-form-field-search-select.component"

@Component({
  selector: "app-abstract-form-field-search-single-select",
  template: "./abstract-form-field-search-single-select.component.html"
})
export abstract class AbstractFormFieldSearchSingleSelect<TValue>
  extends AbstractFormFieldSearchSelect<TValue, TValue|null> implements OnInit {

  ngOnInit() {
    super.ngOnInit();
    this.setSearchToValue()
  }

  abstract areSearchResultsEqual(result1: TValue, result2: TValue): boolean
  abstract isSearchResultType(result: any): result is TValue
  abstract labelFor(result: TValue): string|null
  protected abstract callSearchService(text: string): Observable<TValue[]>
  protected abstract searchResultFromString(value: string): TValue|undefined

  isResultSelected(result: TValue): boolean {
    if (this.isSearchResultType(result) && this.isSearchResultType(this.controlValue)) {
      return this.areSearchResultsEqual(result, this.controlValue)
    }

    return false
  }

  selectResult(result: TValue): void {
    this.controlValue = result
    this.setSearchToValue()
  }

  protected setSearchToValue() {
    const search = (this.controlValue) ? this.labelFor(this.controlValue) : ""
    this.setSearchControlValue(search, false)
  }

  protected handleClearSearchClicked(): void {
    this.clearValue()
    this.clearSearch()
  }

  protected handleSearchResultClicked(result: TValue) {
    this.selectResult(result)
    this.clearSearchResults()
  }

  protected handleSearchValueChange(newValue: string): void {
    this.selectSearchValue()
    super.handleSearchValueChange(newValue)
  }
}
