import {Component, Input, OnInit} from "@angular/core"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {FormControl} from "@angular/forms"
import {KeywordType} from "../../richskill/ApiSkill"

@Component({
  selector: "app-search-multi-select",
  templateUrl: "./search-multi-select.component.html",
  styleUrls: ["./search-multi-select.component.scss"]
})
export class SearchMultiSelectComponent implements OnInit {

  @Input()
  name?: string
  showInput = false
  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  inputFc = new FormControl("")
  results!: string[] | undefined
  @Input()
  keywordType?: KeywordType
  @Input()
  control?: FormControl
  internalSelectedResults: string[] = []
  currentlyLoading = false
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)

  constructor(protected searchService: KeywordSearchService) {
  }

  ngOnInit(): void {
    this.inputFc.valueChanges.subscribe(value => this.getKeywords(value ?? ""))
  }

  selectResult(result: string): void {
    const isResultSelected = this.isResultSelected(result)
    if (!isResultSelected) {
      this.internalSelectedResults.push(result)
    } else {
      this.internalSelectedResults = this.internalSelectedResults.filter(r => r !== result)
    }
    this.control?.patchValue(this.internalSelectedResults)
  }

  private getKeywords(text: string): void {
    this.currentlyLoading = true
    this.keywordType ? this.searchService.searchKeywords(this.keywordType, text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.name).map(r => r.name as string)
        this.currentlyLoading = false
      }) : this.searchService.searchJobcodes(text)
      .subscribe(searchResults => {
        this.results = searchResults.filter(r => !!r && !!r.code && !!r.targetNodeName).map(i => i.targetNodeName ?? "")
        this.currentlyLoading = false
      })
  }

  isResultSelected(result: string): boolean {
    return this.internalSelectedResults.some(i => i === result)
  }

  clearField(): void {
    this.inputFc.patchValue("")
  }

  get showResults(): boolean {
    return this.inputFc.value.length > 0
  }

}
