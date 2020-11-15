import {FormControl, FormGroup} from "@angular/forms";
import {SearchService} from "../search/search.service";
import {ActivatedRoute} from "@angular/router";

export class AbstractSearchComponent {
  searchForm = new FormGroup({
    search: new FormControl("")
  })

  constructor(protected searchService: SearchService, protected route: ActivatedRoute) {
    this.route.queryParams.subscribe(params => {
      const queryString = params.q
      if (queryString && queryString.length > 0) {
        this.searchForm.setValue({search: queryString})
      }
    })
  }

  clearSearch(): boolean {
    this.searchForm.reset()
    return false
  }

  public get searchQuery(): string {
    return this.searchForm.get("search")?.value ?? ""
  }

  handleDefaultSubmit(): boolean {
    return this.submitSkillSearch()
  }

  submitSkillSearch(): boolean {
    if (this.searchQuery.trim().length > 0) {
      this.searchService.simpleSkillSearch(this.searchQuery)
    }
    return false
  }

  submitCollectionSearch(): boolean {
    if (this.searchQuery.trim().length > 0) {
      this.searchService.simpleCollectionSearch(this.searchQuery)
    }
    return false
  }

}
