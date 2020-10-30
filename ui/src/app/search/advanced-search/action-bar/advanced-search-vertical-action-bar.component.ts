import {Component, EventEmitter, OnInit, Output} from "@angular/core"

@Component({
  selector: "app-advanced-search-vertical-action-bar",
  template: `
    <div class="l-actionBarVertical">

      <div class="l-actionBarVertical-x-action">
        <button class="m-button" (click)="searchSkillsClicked.emit()">
          <span class="m-button-x-text">Search Skills</span>
        </button>
      </div>

      <div class="l-actionBarVertical-x-action">
        <button class="m-button" (click)="searchCollectionsClicked.emit()">
          <span class="m-button-x-text">Search Collections</span>
        </button>
      </div>
    </div>
  `
})
export class AdvancedSearchVerticalActionBarComponent implements OnInit {

  @Output() searchSkillsClicked = new EventEmitter<void>()
  @Output() searchCollectionsClicked = new EventEmitter<void>()

  constructor() { }

  ngOnInit(): void {
  }

}
