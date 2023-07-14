import {Component} from "@angular/core"
import {Observable} from "rxjs"
import {map} from "rxjs/operators"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import { IJobCode } from "../../../metadata/job-code/Jobcode"
import {isJobCode} from "./form-field-jobcode-search-select.utilities"
import {areSearchResultsEqual, labelFor, searchResultFromString} from "./form-field-jobcode-search-select.utilities"
import {AbstractFormFieldSearchMultiSelect} from "../abstract-form-field-search-multi-select.component"

@Component({
  selector: "app-form-field-jobcode-search-multi-select",
  templateUrl: "../abstract-form-field-search-multi-select.component.html"
})
export class FormFieldJobCodeSearchMultiSelect extends AbstractFormFieldSearchMultiSelect<IJobCode> {

  constructor(searchService: KeywordSearchService) {
    super(searchService)
  }

  areSearchResultsEqual(result1: IJobCode, result2: IJobCode): boolean {
    return areSearchResultsEqual(result1, result2)
  }

  isSearchResultType(result: any): result is IJobCode {
    return isJobCode(result)
  }

  labelFor(result: IJobCode): string|null {
    return labelFor(result)
  }

  searchResultFromString(value: string): IJobCode|undefined {
    return searchResultFromString(value)
  }

  protected callSearchService(text: string): Observable<IJobCode[]> {
    return this.searchService.searchJobcodes(text).pipe(map(results => results.map(r => r)))
  }
}
