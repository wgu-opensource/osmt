import {Component, Input, OnInit} from "@angular/core"
import {FormField} from "../form-field.component"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {Subscription} from "rxjs"
import {SearchService} from "../../search/search.service"

@Component({
  selector: "app-form-field-search-select",
  templateUrl: "./form-field-search-select.component.html"
})
export class FormFieldSearchSelectComponent extends FormField implements OnInit {

  @Input() type!: "category" | "standard" | "certification" | "alignment" | "employer" | "author"

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)

  queryInProgress!: Subscription
  currentlyLoading = false
  results!: string[] | undefined

  constructor(
    private searchService: SearchService
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

    this.queryInProgress = this.searchService.searchForKeyword(text, this.type)
      .subscribe(searchResults => {
        this.results = searchResults
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
