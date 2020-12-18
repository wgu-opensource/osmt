import {Component, Inject, Input, OnDestroy, OnInit} from "@angular/core"
import {INamedReference, KeywordType} from "../../richskill/ApiSkill"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {Subscription} from "rxjs"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {FormField} from "../form-field.component"

@Component({
  selector: "app-abstract-form-field-search-select",
  template: ``
})
export abstract class AbstractFormFieldSearchSelectComponent extends FormField implements OnInit, OnDestroy {

  @Input() keywordType!: KeywordType

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)

  queryInProgress!: Subscription
  currentlyLoading = false
  results!: string[] | undefined

  protected constructor(
    protected searchService: KeywordSearchService
  ) {
    super()
  }

  ngOnInit(): void {
    this.control.valueChanges.subscribe(next => { this.performSearch(next) })
  }

  ngOnDestroy(): void {
    this.queryInProgress?.unsubscribe()
  }

  abstract selectResult(result: string): void
  abstract isResultSelected(result: string): boolean

  get currentCategory(): string {
    return this.control.value
  }

  performSearch(text: string): void {
    if (!text || !this.keywordType) {
      return // no search to perform
    }
    this.currentlyLoading = true

    if (this.queryInProgress) {
      this.queryInProgress.unsubscribe() // unsub to existing query first
    }

    this.queryInProgress = this.searchService.searchKeywords(this.keywordType, text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.name).map(r => r.name as string)
        this.currentlyLoading = false
      })
  }

  get showResults(): boolean {
    const isEmpty = this.valueFromControl?.trim()?.length <= 0
    return !isEmpty && this.results !== undefined
  }
}
