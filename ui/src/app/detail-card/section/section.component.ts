import { Component, Input, OnInit } from "@angular/core"

export interface IDetailCardSectionData {
  label: string
  bodyHtml: string
  showIfEmpty: boolean
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
    bodyHtml: "",
    showIfEmpty: false
  }

  constructor() {}
  ngOnInit(): void {}

}
