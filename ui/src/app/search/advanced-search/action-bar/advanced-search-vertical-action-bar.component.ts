import {Component} from "@angular/core"
import {AbstractAdvancedSearchActionBarComponent} from "./abstract-advanced-search-action-bar.component"

@Component({
  selector: "app-advanced-search-vertical-action-bar",
  template: `
    <div class="l-actionBarVertical">

      <div class="l-actionBarVertical-x-action">
        <button class="m-button" (click)="skillButtonClicked()">
          <span class="m-button-x-text">{{skillButtonText}}</span>
        </button>
      </div>

      <div class="l-actionBarVertical-x-action">
        <button class="m-button" (click)="collectionButtonClicked()">
          <span class="m-button-x-text">{{collectionButtonText}}</span>
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
