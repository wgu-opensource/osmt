import {Component, Input} from "@angular/core"
import {Observable} from "rxjs"
import {map} from "rxjs/operators"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import {IAlignment, INamedReference, KeywordType} from "../../../richskill/ApiSkill"
import {isAlignment, isAlignmentKeywordType, searchServiceResultToAlignment} from "./form-field-keyword-search-select.utilities"
import {isNamedReference, isNamedReferenceKeywordType, searchServiceResultToNamedReference} from "./form-field-keyword-search-select.utilities"
import {isStringKeyword, isStringKeywordKeywordType, searchServiceResultToStringKeyword} from "./form-field-keyword-search-select.utilities"
import {areSearchResultsEqual, labelFor, searchResultFromString} from "./form-field-keyword-search-select.utilities"
import {AbstractFormFieldSearchSingleSelect} from "../abstract-form-field-search-single-select.component"

@Component({
  selector: "app-form-field-keyword-search-select",
  templateUrl: "../abstract-form-field-search-single-select.component.html"
})
export class FormFieldKeywordSearchSelect
  extends AbstractFormFieldSearchSingleSelect<IAlignment|INamedReference|string> {

  @Input() keywordType: KeywordType | null = null

  constructor(searchService: KeywordSearchService) {
    super(searchService)
  }

  get isAlignmentKeywordType(): boolean {
    return (this.keywordType) ? isAlignmentKeywordType(this.keywordType) : false
  }

  get isNamedReferenceKeywordType(): boolean {
    return (this.keywordType) ? isNamedReferenceKeywordType(this.keywordType) : false
  }

  get isStringKeywordType(): boolean {
    return (this.keywordType) ? isStringKeywordKeywordType(this.keywordType) : false
  }

  areSearchResultsEqual(
    result1: IAlignment|INamedReference|string,
    result2: IAlignment|INamedReference|string
  ): boolean {
    return areSearchResultsEqual(result1, result2)
  }

  isSearchResultType(result: any): result is IAlignment|INamedReference|string {
    return isAlignment(result) || isNamedReference(result) || isStringKeyword(result)
  }

  labelFor(value: IAlignment|INamedReference|string): string|null {
    return (value) ? labelFor(value) : null
  }

  searchResultFromString(value: string): IAlignment|INamedReference|string|undefined {
    if (!this.keywordType) {
      return  undefined
    }

    return searchResultFromString(this.keywordType, value)
  }

  protected callSearchService(text: string): Observable<(IAlignment|INamedReference|string)[]> {
    if (!this.keywordType) {
      throw new Error("KeywordType is not set")
    }

    let transformFn = (r: INamedReference|string|undefined): IAlignment|INamedReference|string|undefined => r

    if (this.isAlignmentKeywordType) {
      transformFn = searchServiceResultToAlignment
    } else if (this.isNamedReferenceKeywordType) {
      transformFn = searchServiceResultToNamedReference
    } else if (this.isStringKeywordType) {
      transformFn = searchServiceResultToStringKeyword
    }

    return this.searchService.searchKeywords(this.keywordType, text).pipe(
      map((results): (IAlignment|INamedReference|string)[] => results.flatMap(r => transformFn(r) ?? []))
    )
  }
}
