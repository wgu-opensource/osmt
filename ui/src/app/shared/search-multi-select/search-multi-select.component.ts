import {Component, Input, OnInit} from "@angular/core"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {FormControl} from "@angular/forms"
import {ApiNamedReference, KeywordType} from "../../richskill/ApiSkill"
import {ApiJobCode} from "../../metadata/job-codes/Jobcode"
import {FilterSearchComponent} from "@shared/filter-search/filter-search.component"

@Component({
  selector: "app-search-multi-select",
  templateUrl: "./search-multi-select.component.html",
  styleUrls: ["./search-multi-select.component.scss"]
})
export class SearchMultiSelectComponent extends FilterSearchComponent implements OnInit {

  @Input()
  name?: string
  showInput = false
  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  inputFc = new FormControl("")
  results!: ApiNamedReference[] | ApiJobCode[] | undefined
  @Input()
  keywordType?: KeywordType
  @Input()
  control?: FormControl
  currentlyLoading = false
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)

  constructor(protected searchService: KeywordSearchService) {
    super()
  }

  ngOnInit(): void {
    this.getKeywords("")
    this.inputFc.valueChanges.subscribe(value => this.getKeywords(value ?? ""))
  }

  selectResult(result: ApiJobCode | ApiNamedReference): void {
    const isResultSelected = this.isResultSelected(result)
    if (!isResultSelected) {
      this.control?.value.push(result)
    } else {
      const newValues = this.control?.value.filter((r: ApiJobCode | ApiNamedReference) => !this.areResultsEqual(r, result))
      this.control?.patchValue(newValues)
    }
  }

  private getKeywords(text: string): void {
    this.currentlyLoading = true
    this.keywordType ? this.searchService.searchKeywords(this.keywordType, text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.name)
        this.currentlyLoading = false
      }) : this.searchService.searchJobcodes(text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.code && !!r.targetNodeName)
        this.currentlyLoading = false
      })
  }

  isResultSelected(result: ApiJobCode | ApiNamedReference): boolean {
    return this.control?.value.some((i: ApiJobCode | ApiNamedReference) => this.areResultsEqual(i, result))
  }

  clearField(): void {
    this.inputFc.patchValue("")
  }

}
