import {Component, Input} from "@angular/core"
import {AbstractCreateCollectionActionbarComponent} from "./abstract-create-collection-actionbar.component"

@Component({
  selector: "app-create-collection-action-bar-horizontal",
  template: `
    <nav class="l-flex l-flex-alignCenter l-flex-0 t-elevation-large">
      <button class="m-actionBarItemHorizontal m-actionBarItemHorizontal-secondary" type="button" (click)="handleCancel()">
        <svg class="m-actionBarItemHorizontal-x-icon t-icon">
          <use [attr.xlink:href]="cancelIcon"></use>
        </svg>
        <span class="m-actionBarItemHorizontal-x-label">Cancel</span>
        <span class="m-actionBarItemHorizontal-x-divider m-divider m-divider-large"></span>
      </button>

      <app-formfield-submit
        [formGroup]="collectionForm"
        [observables]="[collectionSaved]"
        (errorsOccurred)="handleFormErrors($event)"
        [mobileView]="true"
      ></app-formfield-submit>
    </nav>
  `
})
export class CreateCollectionActionBarHorizontalComponent extends AbstractCreateCollectionActionbarComponent {

  constructor() {
    super()
  }

  handleFormErrors(error: any): void {
  }
}
