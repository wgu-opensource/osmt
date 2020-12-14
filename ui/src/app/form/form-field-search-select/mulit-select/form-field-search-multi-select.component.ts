import {Component, OnInit, Output, EventEmitter, OnDestroy} from "@angular/core"
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

  isResultSelected(result: string): boolean {
    return !!this.internalSelectedResults.find(value => value === result)
  }

  selectResult(result: string): void {
    if (!this.isResultSelected(result)) {
      this.internalSelectedResults.push(result)
    }
    this.currentSelection.emit(this.internalSelectedResults)
  }

  unselectResult(result: string): void {
    this.internalSelectedResults = this.internalSelectedResults.filter(r => r !== result)
    console.log(`Removing result... remaining ${this.internalSelectedResults}`)
    this.currentSelection.emit(this.internalSelectedResults)
  }
}
