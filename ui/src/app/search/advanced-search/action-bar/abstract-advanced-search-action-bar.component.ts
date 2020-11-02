import {Component, OnInit, Output, EventEmitter} from "@angular/core"

@Component({
  selector: "app-abstract-advanced-search-action-bar",
  template: ""
})
export class AbstractAdvancedSearchActionBarComponent implements OnInit {

  @Output() searchSkillsClicked = new EventEmitter<void>()
  @Output() searchCollectionsClicked = new EventEmitter<void>()

  skillButtonText = "Search Skills"
  collectionButtonText = "Search Collections"

  constructor() { }

  ngOnInit(): void {
  }

  skillButtonClicked(): void {
    this.searchSkillsClicked.emit()
  }

  collectionButtonClicked(): void {
    this.searchCollectionsClicked.emit()
  }
}
