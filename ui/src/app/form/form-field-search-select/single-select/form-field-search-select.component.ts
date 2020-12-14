import {Component} from "@angular/core"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import {AbstractFormFieldSearchSelectComponent} from "../abstract-form-field-search-select.component"

@Component({
  selector: "app-form-field-search-select",
  templateUrl: "./form-field-search-select.component.html"
})
export class FormFieldSearchSelectComponent extends AbstractFormFieldSearchSelectComponent {

  constructor(searchService: KeywordSearchService) {
    super(searchService)
  }

  selectResult(result: string): void {
    this.control.setValue(result, {emitEvent: false})
    this.results = undefined
  }

  isResultSelected(result: string): boolean {
    return this.valueFromControl === result
  }
}
