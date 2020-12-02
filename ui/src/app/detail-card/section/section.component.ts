import { Component, Input, OnInit } from "@angular/core"

export interface IDetailCardSectionData {
  label: string
  bodyHtml: string
  showIfEmpty: boolean
}

@Component({
  selector: "app-detail-card-section",
  template: `
  <div class="t-margin-extraSmall t-margin-bottom">
    <div class="m-sectionLabel">{{data.label}}</div>
  </div>
  <p class="t-type-body" [innerHTML]="data.bodyHtml"></p>
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
