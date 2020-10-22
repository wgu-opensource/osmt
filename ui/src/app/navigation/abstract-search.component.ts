import {OnInit} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";

export class AbstractSearchComponent {
  searchForm = new FormGroup({
    search: new FormControl("")
  })

  constructor() {

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
    console.log("Search Skills", this.searchQuery)
    return false
  }

  submitCollectionSearch(): boolean {
    console.log("Search Collections", this.searchQuery)
    return false
  }
}
