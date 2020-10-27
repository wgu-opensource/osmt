import {OnInit} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {SearchService} from "../search/search.service";

export class AbstractSearchComponent {
  searchForm = new FormGroup({
    search: new FormControl("")
  })

  constructor(protected searchService: SearchService) {

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
    this.searchService.simpleSkillSearch(this.searchQuery)
    return false
  }

  submitCollectionSearch(): boolean {
    console.log("Search Collections", this.searchQuery)
    return false
  }
}
