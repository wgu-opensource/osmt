import {Component, EventEmitter, OnDestroy, OnInit, Output} from "@angular/core"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import {AbstractFormFieldSearchSelectComponent} from "../abstract-form-field-search-select.component"

@Component({
  selector: "app-form-field-search-multi-select",
  templateUrl: "./form-field-search-multi-select.component.html"
})
export class FormFieldSearchMultiSelectComponent extends AbstractFormFieldSearchSelectComponent implements OnInit, OnDestroy {

  @Output() currentSelection = new EventEmitter<string[]>()

  internalSelectedResults: string[] = []

  constructor(searchService: KeywordSearchService) {
    super(searchService)
  }

  ngOnInit(): void {
    super.ngOnInit()
    this.performInitialSearchAndPopulation()
  }

  performInitialSearchAndPopulation(): void {
    const value = this.control.value as string
    this.control.setValue("")
    this.internalSelectedResults = value.split(";").map(s => s.trim()).filter(s => s.length > 0)
    this.emitCurrentSelection()
  }

  get showResults(): boolean {
    const isEmpty = this.valueFromControl?.trim()?.length <= 0
    const isDirty = this.control.dirty
    return isDirty && !isEmpty && this.results !== undefined
  }

  isResultSelected(result: string): boolean {
    return !!this.internalSelectedResults.find(value => value === result)
  }

  selectResult(result: string): void {
    if (!this.isResultSelected(result)) {
      this.internalSelectedResults.push(result)
    }
    this.emitCurrentSelection()
  }

  unselectResult(result: string): void {
    this.internalSelectedResults = this.internalSelectedResults.filter(r => r !== result)
    this.control.markAsDirty()
    this.emitCurrentSelection()
  }

  private emitCurrentSelection(): void {
    this.currentSelection.emit(this.internalSelectedResults)
  }

  handleKeyDownEnter($event: any): boolean {
    return false
  }
}
