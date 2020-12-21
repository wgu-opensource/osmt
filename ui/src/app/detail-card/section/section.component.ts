import {Component, Input, OnInit, TemplateRef} from "@angular/core"

export interface IDetailCardSectionData {
  label: string
  bodyString?: string
  bodyTemplate?: TemplateRef<any>
  showIfEmpty: boolean
}

@Component({
  selector: "app-detail-card-section",
  template: `
    <div class="t-margin-extraSmall t-margin-bottom">
      <div class="m-sectionLabel">{{data.label}}</div>
    </div>
    <ng-template [ngIf]="!!data.bodyString">
      <p class="t-type-body" [innerHTML]="data.bodyString"></p>
    </ng-template>
    <ng-template [ngIf]="!!data.bodyTemplate">
      <ng-container *ngTemplateOutlet="data.bodyTemplate"></ng-container>
    </ng-template>
  `
})
export class DetailCardSectionComponent {

  @Input() data: IDetailCardSectionData = {
    label: "",
    bodyString: undefined,
    bodyTemplate: undefined,
    showIfEmpty: false
  }

  constructor() {}
}
