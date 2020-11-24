import {Component, Input} from "@angular/core"
import {AbstractCreateCollectionActionbarComponent} from "./abstract-create-collection-actionbar.component"

@Component({
  selector: "app-create-collection-action-bar-vertical",
  template: `
    <div class="l-actionBarVertical">
      <div class="l-actionBarVertical-x-action">
        <app-formfield-submit
          [formGroup]="collectionForm"
          [observables]="[collectionSaved]"
          (errorsOccurred)="handleFormErrors($event)"
        ></app-formfield-submit>
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

  constructor() {
    super()
  }

  handleFormErrors(error: any): void {
  }
}
