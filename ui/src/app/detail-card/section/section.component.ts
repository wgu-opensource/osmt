import { Component, Input, OnInit, ViewEncapsulation } from "@angular/core"

export interface IDetailCardSectionData {
  label: string
  bodyHtml: string
}

@Component({
  selector: "app-detail-card-section",
  template: `
  <div>
    <div class="t-verticalSpacing-extraSmall">
      <div class="m-sectionLabel">{{data.label}}</div>
    </div>
    <div class="t-verticalSpacing-medium">
      <p class="t-type-body" [innerHTML]="data.bodyHtml" ></p>
   </div>
  </div>
  `
})
export class DetailCardSectionComponent implements OnInit {

  @Input() data: IDetailCardSectionData = {
    label: "",
    bodyHtml: ""
  }

  constructor() {}
  ngOnInit(): void {}

}
