import { Component } from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {AbstractCreateCollectionActionbarComponent} from "./abstract-create-collection-actionbar.component"

@Component({
  selector: "app-create-collection-action-bar-vertical",
  template: `
    <div class="l-actionBarVertical">

      <div class="l-actionBarVertical-x-action">
        <button class="m-button" (click)="handleSave()">
          <span class="m-button-x-text">Save</span>
        </button>
      </div>

      <button class="m-actionBarItem" type="button" (click)="handleCancel()">
        <span class="m-actionBarItem-x-icon">
          <svg class="t-icon" aria-hidden="true">
            <use [attr.xlink:href]="cancelIcon"></use>
          </svg>
        </span>
        <span class="m-actionBarItem-x-text">Cancel</span>
      </button>
    </div>
  `
})
export class CreateCollectionActionBarVerticalComponent extends AbstractCreateCollectionActionbarComponent {

  cancelIcon = SvgHelper.path(SvgIcon.CANCEL)

  constructor() {
    super()
  }

}
