import {Component, OnInit, Output, EventEmitter, Input} from "@angular/core"

@Component({
  selector: "app-abstract-advanced-search-action-bar",
  template: ""
})
export class AbstractAdvancedSearchActionBarComponent implements OnInit {

  @Input() searchSkillsDisabled = false
  @Input() searchCollectionsDisabled = false
  @Output() searchSkillsClicked = new EventEmitter<void>()
  @Output() searchCollectionsClicked = new EventEmitter<void>()

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
