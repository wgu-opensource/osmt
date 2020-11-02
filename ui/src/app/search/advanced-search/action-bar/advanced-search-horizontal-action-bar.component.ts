import {Component, OnInit, Output, EventEmitter} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {AbstractAdvancedSearchActionBarComponent} from "./abstract-advanced-search-action-bar.component"

@Component({
  selector: "app-advanced-search-horizontal-action-bar",
  template: `
    <nav class="l-flex l-flex-alignCenter l-flex-0 t-elevation-large">
      <button class="m-actionBarItemHorizontal" (click)="searchSkillsClicked.emit()">
        <svg class="m-actionBarItemHorizontal-x-icon t-icon">
          <use [attr.xlink:href]="iconDismiss"></use>
        </svg>
        <span class="m-actionBarItemHorizontal-x-label">Search Skills</span>
        <span class="m-actionBarItemHorizontal-x-divider m-divider m-divider-large"></span>
      </button>

      <button class="m-actionBarItemHorizontal" (click)="searchCollectionsClicked.emit()">
        <svg class="m-actionBarItemHorizontal-x-icon t-icon">
          <use [attr.xlink:href]="iconDismiss"></use>
        </svg>
        <span class="m-actionBarItemHorizontal-x-label">Search Collections</span>
        <span class="m-actionBarItemHorizontal-x-divider m-divider m-divider-large"></span>
      </button>
    </nav>
  `,
})
export class AdvancedSearchHorizontalActionBarComponent extends AbstractAdvancedSearchActionBarComponent {

  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)

  constructor() {
    super()
  }
}
