import { Component, OnInit } from "@angular/core"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import {FormFieldSearchSelectComponent} from "../.."
import {ApiJobCode} from "../../../job-codes/Jobcode"

@Component({
  selector: "app-jobcode-single-select",
  templateUrl: "../../form-field-search-select/single-select/form-field-search-select.component.html",
})
export class JobcodeSingleSelectComponent extends FormFieldSearchSelectComponent {

  constructor(
    searchService: KeywordSearchService
  ) {
    super(searchService)
  }

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
        this.results = searchResults.filter(r => !!r && !!r.targetNodeName).map(r => r.code + "|" + r.targetNodeName)
        this.currentlyLoading = false
      })
  }

  selectResult(result: string): void {
    this.control.setValue(result.split("|")[1], {emitEvent: false})
    this.results = undefined
  }

}
