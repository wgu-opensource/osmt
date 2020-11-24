import {Component, Output, EventEmitter} from "@angular/core"
import {AbstractCreateCollectionActionbarComponent} from "./abstract-create-collection-actionbar.component"

@Component({
  selector: " app-create-collection-action-bar-vertical",
  template: `
    <div class="l-actionBarVertical">
      <div class="l-actionBarVertical-x-action">
        <app-formfield-submit
          [formGroup]="collectionForm"
          [observables]="[collectionSaved]"
          (errorsOccurred)="handleFormErrors($event)"
        ></app-formfield-submit>
      </div>

      <nav class="m-quickLinks" aria-labelledby="save-quicklinks">
        <h3 class="t-visuallyHidden" id="save-quicklinks">Quick Links</h3>
        <a (click)="scrollToTopClicked.emit()">Back to top</a>
      </nav>

      <button class="m-actionBarItem" type="button" (click)="handleCancel()">
        <span class="m-actionBarItem-x-icon">
          <svg class="t-icon" aria-hidden="true">
            <use [attr.xlink:href]="cancelIcon"></use>
          </svg>
        </span>
        <span class="m-actionBarItem-x-text">Cancel</span>
      </button>

      <div class="m-actionBartVertical-x-message" *ngIf="!formValid">
        <p>Additional information required before you can save. Go to <a class="t-link" (click)="showMissingFields.emit()">first required missing field</a>.</p>
      </div>
    </div>
  `
})
export class CreateCollectionActionBarVerticalComponent extends AbstractCreateCollectionActionbarComponent {

  @Output() showMissingFields = new EventEmitter<void>()

  constructor() {
    super()
  }

  handleFormErrors(error: any): void {
  }
}
