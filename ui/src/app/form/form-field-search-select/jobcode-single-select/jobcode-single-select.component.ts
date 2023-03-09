import { Component, OnInit } from "@angular/core"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import {FormFieldSearchSelectComponent} from "../.."

@Component({
  selector: "app-jobcode-single-select",
  templateUrl: "../../form-field-search-select/single-select/form-field-search-select.component.html",
  styleUrls: ["./jobcode-single-select.component.scss"]
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
        this.results = searchResults.filter(r => !!r && !!r.targetNodeName).map(r => r.targetNodeName as string)
        this.currentlyLoading = false
      })
  }

}
