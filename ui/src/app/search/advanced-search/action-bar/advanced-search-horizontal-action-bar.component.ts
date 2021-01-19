import {Component} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {AbstractAdvancedSearchActionBarComponent} from "./abstract-advanced-search-action-bar.component"

@Component({
  selector: "app-advanced-search-horizontal-action-bar",
  template: `
    <nav class="l-flex l-flex-alignCenter l-flex-0 t-elevation-large">
      <button class="m-actionBarItemHorizontal" (click)="skillButtonClicked()">
        <span class="m-actionBarItemHorizontal-x-icon">
          <svg aria-hidden="true">
            <use [attr.xlink:href]="iconSearch"></use>
          </svg>
        </span>
        <span class="m-actionBarItemHorizontal-x-label">Search RSD<span class="t-type-lowercase">s</span></span>
        <span class="m-actionBarItemHorizontal-x-divider m-divider m-divider-large"></span>
      </button>

      <button class="m-actionBarItemHorizontal" (click)="collectionButtonClicked()">
        <span class="m-actionBarItemHorizontal-x-icon">
          <svg aria-hidden="true">
            <use [attr.xlink:href]="iconSearch"></use>
          </svg>
        </span>
        <span class="m-actionBarItemHorizontal-x-label">Search Collections</span>
        <span class="m-actionBarItemHorizontal-x-divider m-divider m-divider-large"></span>
      </button>
    </nav>
  `
})
export class AdvancedSearchHorizontalActionBarComponent extends AbstractAdvancedSearchActionBarComponent {

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)

  constructor() {
    super()
  }
}
