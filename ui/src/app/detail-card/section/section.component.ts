import { Component, Input, OnInit, TemplateRef } from "@angular/core";

export interface IDetailCardSectionData {
  label: string
  bodyString?: string
  bodyTemplate?: TemplateRef<any>
  showIfEmpty: boolean
}

@Component({
  selector: "app-detail-card-section",
  templateUrl: "./section.component.html"
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
