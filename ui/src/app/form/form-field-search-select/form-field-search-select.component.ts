import {Component, Input, OnInit} from "@angular/core"
import {FormField} from "../form-field.component"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {Subscription} from "rxjs"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {KeywordType} from "../../richskill/ApiSkill"

@Component({
  selector: "app-form-field-search-select",
  templateUrl: "./form-field-search-select.component.html"
})
export class FormFieldSearchSelectComponent extends FormField implements OnInit {

  @Input() type!: KeywordType

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)

  queryInProgress!: Subscription
  currentlyLoading = false
  results!: string[] | undefined

  constructor(
    private searchService: KeywordSearchService
  ) {
    super()
  }

  ngOnInit(): void {
    this.control.valueChanges.subscribe(next => { this.performSearch(next) })
  }

  get currentCategory(): string {
    return this.control.value
  }

  performSearch(text: string): void {
    this.currentlyLoading = true

    if (this.queryInProgress) {
      this.queryInProgress.unsubscribe() // unsub to existing query first
    }

    this.queryInProgress = this.searchService.searchKeywords(this.type, text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.name).map(r => r.name as string)
        this.currentlyLoading = false
      })
  }

  selectResult(result: string): void {
    this.control.setValue(result, {emitEvent: false})
    this.results = undefined
  }

  isResultSelected(result: string): boolean {
    return this.valueFromControl === result
  }

  get showResults(): boolean {
    const isEmpty = this.valueFromControl?.trim()?.length <= 0
    return !isEmpty && this.results !== undefined
  }
}
