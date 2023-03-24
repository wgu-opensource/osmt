import {Component} from "@angular/core"
import {Observable} from "rxjs"
import {AbstractFormFieldSearchSelect} from "./abstract-form-field-search-select.component"

@Component({
  selector: "app-abstract-form-field-search-multi-select",
  template: "./abstract-form-field-search-multi-select.component.html"
})
export abstract class AbstractFormFieldSearchMultiSelect<TValue>
  extends AbstractFormFieldSearchSelect<TValue, TValue[]|null> {

  abstract areSearchResultsEqual(result1: TValue, result2: TValue): boolean
  abstract isSearchResultType(result: any): result is TValue
  abstract labelFor(result: TValue): string|null
  protected abstract callSearchService(text: string): Observable<TValue[]>
  protected abstract searchResultFromString(value: string): TValue|undefined

  get selectedResults(): TValue[] {
    return this.controlValue?.map(r => r) ?? []
  }

  isResultSelected(result: TValue): boolean {
    if (this.isSearchResultType(result)) {
      return !!(this.controlValue?.find((r: TValue) => this.areSearchResultsEqual(r, result)))
    }

    return false
  }

  selectResult(result: TValue): void {
    if (this.isSearchResultType(result) && !this.isResultSelected(result)){
      let selected: TValue[] = this.controlValue ?? []
      selected.push(result)
      this.controlValue = selected
      this.clearSearch()
    }
  }

  unselectResult(result: TValue): void {
    if (this.isSearchResultType(result)){
      let selected = this.controlValue?.filter((r: TValue) => !this.areSearchResultsEqual(r, result)) ?? []
      this.controlValue = (selected.length > 0) ? selected : null
    }
  }

  onSelectedResultClicked(result: TValue): void {
    this.handleSelectedResultClicked(result)
  }

  protected handleClearSearchClicked(): void {
    this.clearSearch()
  }

  protected handleSearchResultClicked(result: TValue): void {
    this.selectResult(result)
  }

  protected handleSelectedResultClicked(result: TValue): void {
    this.unselectResult(result)
  }
}
