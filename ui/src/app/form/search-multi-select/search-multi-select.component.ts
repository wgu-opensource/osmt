import {Component, OnInit} from "@angular/core"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {
  FormFieldSearchMultiSelectComponent
} from "../form-field-search-select/mulit-select/form-field-search-multi-select.component"

@Component({
  selector: "app-search-multi-select",
  templateUrl: "./search-multi-select.component.html",
  styleUrls: ["./search-multi-select.component.scss"]
})
export class SearchMultiSelectComponent extends FormFieldSearchMultiSelectComponent implements OnInit {

  constructor(protected searchService: KeywordSearchService) {
    super(searchService)
  }

  ngOnInit(): void {
    super.ngOnInit()
    this.performInitialSearchAndPopulation()
  }

  selectResult(result: string): void {
    const isResultSelected = this.isResultSelected(result)
    if (!isResultSelected) {
      this.internalSelectedResults.push(result)
    } else {
      this.internalSelectedResults = this.internalSelectedResults.filter(r => r !== result)
    }
    super.emitCurrentSelection()
  }

  performSearch(text: string): void {
    if (!text) {
      return // no search to perform
    }
    this.currentlyLoading = true

    if (this.queryInProgress) {
      this.queryInProgress.unsubscribe() // unsub to existing query first
    }

    this.queryInProgress = this.keywordType ?  this.searchService.searchKeywords(this.keywordType, text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.name).map(r => r.name as string)
        this.currentlyLoading = false
      }) : this.searchService.searchJobcodes(text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.code && !!r.targetNodeName).map(i => i.targetNodeName ?? "")
        this.currentlyLoading = false
      })
  }

}
