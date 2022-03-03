import {Component} from "@angular/core"
import {AbstractAdvancedSearchActionBarComponent} from "./abstract-advanced-search-action-bar.component"

@Component({
  selector: "app-advanced-search-vertical-action-bar",
  template: `
    <div class="l-actionBarVertical">

      <div class="l-actionBarVertical-x-action">
        <button class="m-button" [disabled]="searchSkillsDisabled" (click)="skillButtonClicked()">
          <span class="m-button-x-text">Search RSD<span class="t-type-lowercase">s</span></span>
        </button>
      </div>

      <div class="l-actionBarVertical-x-action">
        <button class="m-button" [disabled]="searchCollectionsDisabled" (click)="collectionButtonClicked()">
          <span class="m-button-x-text">Search Collections</span>
        </button>
      </div>
    </div>
  `
})
export class AdvancedSearchVerticalActionBarComponent extends AbstractAdvancedSearchActionBarComponent {

  constructor() {
    super()
  }
}
